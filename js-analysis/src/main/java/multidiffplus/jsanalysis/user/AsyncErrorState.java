package multidiffplus.jsanalysis.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Name;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import multidiffplus.cfg.CFG;
import multidiffplus.cfg.CFGEdge;
import multidiffplus.cfg.IBuiltinState;
import multidiffplus.cfg.IUserState;
import multidiffplus.jsanalysis.abstractdomain.BValue;
import multidiffplus.jsanalysis.abstractdomain.Change;
import multidiffplus.jsanalysis.abstractdomain.Criterion;
import multidiffplus.jsanalysis.abstractdomain.Str;
import multidiffplus.jsanalysis.abstractdomain.Variable;
import multidiffplus.jsanalysis.flow.JavaScriptAnalysisState;
import multidiffplus.jsanalysis.interpreter.ExpEval;

public class AsyncErrorState implements IUserState {

    /**
     * Tracks the async call sites that ran the callback function.
     */
    private Map<FunctionCall, Criterion> asyncCallsites;

    /**
     * The error from the asynchronous function.
     */
    private Pair<Variable, Criterion> error;

    /**
     * The return values from the asynchronous function.
     */
    private Map<Variable, Criterion> returnValues;

    private AsyncErrorState() {
	this.asyncCallsites = new HashMap<>();
	this.error = null;
	this.returnValues = new HashMap<>();
    }

    private AsyncErrorState(FunctionCall fc, Pair<Variable, Criterion> error,
	    Map<Variable, Criterion> returnValues) {
	this.asyncCallsites = new HashMap<>();
	this.asyncCallsites.put(fc, Criterion.of(fc, Criterion.Type.ASYNC_ERROR_CALL_SITE));
	this.error = error;
	this.returnValues = returnValues;
    }

    private AsyncErrorState(Map<FunctionCall, Criterion> asyncCallsites,
	    Pair<Variable, Criterion> error, Map<Variable, Criterion> returnValues) {
	this.asyncCallsites = asyncCallsites;
	this.error = error;
	this.returnValues = returnValues;
    }

    @Override
    public IUserState interpretStatement(IBuiltinState builtin, ClassifiedASTNode statement) {
	if (asyncCallsites.isEmpty() || statement instanceof FunctionNode) {
	    // We are not inside an async callsite.
	    return this;
	}

	// Look up the BValue of the parameter e === null || e !== null
	AstNode node = (AstNode) statement;
	ExpEval eval = ((JavaScriptAnalysisState) builtin).getExpressionEvaluator();
	BValue val = eval.resolveValue(new Name(0, error.getKey().name));

	if (val.stringAD.le == Str.LatticeElement.SBLANK
		|| val.stringAD.le == Str.LatticeElement.BOTTOM) {
	    // The error parameter is empty, so this statement is safe.
	    return this;
	}

	Set<Criterion> usedParams = ParamUseVisitor.findUsedParams(node, returnValues);
	if (usedParams.isEmpty()) {
	    // No parameters are used within the statement.
	    return this;
	}

	// The error parameter may be non-null.
	for (Criterion criterion : asyncCallsites.values()) {
	    node.addDependency(criterion.getType().toString(), criterion.getId());
	}
	for (Criterion criterion : usedParams) {
	    node.addDependency(criterion.getType().toString(), criterion.getId());
	}
	node.addDependency(error.getValue().getType().toString(), error.getValue().getId());

	return this;
    }

    @Override
    public IUserState interpretBranchCondition(IBuiltinState builtin, CFGEdge edge) {
	return this;
    }

    @Override
    public IUserState interpretCallSite(IBuiltinState builtin, ClassifiedASTNode callSite,
	    List<IUserState> functionExitStates) {
	return this;
    }

    @Override
    public IUserState initializeCallback(IBuiltinState builtin, ClassifiedASTNode callSite,
	    CFG function) {

	JavaScriptAnalysisState jsBuiltin = (JavaScriptAnalysisState) builtin;

	FunctionCall fc = (FunctionCall) callSite;

	// TODO: Resolve the target and callback function to see if either have changed.

	if (!(Change.test(fc) || Change.testU(fc.getTarget()))) {
	    // The call site is not new and the target has not changed.
	    return new AsyncErrorState();
	}

	if (!(fc.getTarget() instanceof InfixExpression)
		|| !(((InfixExpression) fc.getTarget()).getRight() instanceof Name)) {
	    // The target is not in the format 'API.function()'
	    return this;
	}

	// Resolve the object where the function is defined.
	InfixExpression target = (InfixExpression) fc.getTarget();
	Name funct = (Name) target.getRight();
	ExpEval eval = jsBuiltin.getExpressionEvaluator();
	BValue targetObject = eval.eval(target.getLeft());

	// Is the criterion an API the checker targets?
	boolean targetOfInterest = false;
	for (Criterion criterion : targetObject.deps.getDependencies()) {
	    if (criterion.getType() == Criterion.Type.VALUE
		    && criterion.getNode().toSource().equals("require('fs')")
		    && funct.toSource().equals("readFile")) {
		Criterion api = Criterion.of(criterion.getNode(), Criterion.Type.ASYNC_ERROR_API);
		criterion.getNode().addCriterion(api.getType().toString(), api.getId());
		targetOfInterest = true;
	    }
	}

	if (!targetOfInterest) {
	    // This is not a call site of interest.
	    return new AsyncErrorState();
	}

	FunctionNode f = (FunctionNode) function.getEntryNode().getStatement();
	Pair<Variable, Criterion> error = null;
	Map<Variable, Criterion> params = new HashMap<>();
	int i = 0;
	for (AstNode param : f.getParams()) {
	    if (i == 0) {
		Variable v = jsBuiltin.getUnderlyingState().env.apply((Name) param);
		error = Pair.of(v, Criterion.of(param, Criterion.Type.ASYNC_ERROR_EPARAM));
	    } else {
		Variable v = jsBuiltin.getUnderlyingState().env.apply((Name) param);
		params.put(v, Criterion.of(param, Criterion.Type.ASYNC_ERROR_VPARAM));
	    }
	    i++;
	}
	return new AsyncErrorState(fc, error, params);
    }

    @Override
    public IUserState join(IUserState that) {
	Map<FunctionCall, Criterion> newAsyncCallsites = new HashMap<>();
	newAsyncCallsites.putAll(this.asyncCallsites);
	newAsyncCallsites.putAll(((AsyncErrorState) that).asyncCallsites);
	return new AsyncErrorState(newAsyncCallsites, error, returnValues);
    }

    @Override
    public boolean equivalentTo(IUserState that) {
	if (!this.asyncCallsites.keySet()
		.containsAll(((AsyncErrorState) that).asyncCallsites.keySet())) {
	    // that contains elements that this does not contain.
	    return false;
	}
	if (!((AsyncErrorState) that).asyncCallsites.keySet()
		.containsAll(this.asyncCallsites.keySet())) {
	    // this contains elements that that does not contain.
	    return false;
	}
	return true;
    }

    /**
     * Creates an initial SyncErrorState for a script.
     * 
     * This should only be called once per analysis. All other states should be
     * created by either interpreting statements or joining two states.
     */
    public static AsyncErrorState initializeScriptState() {
	return new AsyncErrorState();
    }

}