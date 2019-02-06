package multidiffplus.jsanalysis.flow;

/**
 * Runs an analysis of a JavaScript function.
 */
public class Analysis {

    // /** A stack frame that needs to be loaded. **/
    // private StackFrame frameToLoad;
    //
    // /** {@code true} if a new frame was placed on the stack during transfer. **/
    // private boolean newFrameLoaded;
    //
    // /** How long the analysis should run before timing out. **/
    // private static final long TIMEOUT = 6000;
    //
    // /** Used for timing out the analysis. **/
    // private StopWatch timer = new StopWatch();
    //
    // /** A reference to the list of CFGs to use for executing methods. **/
    // public Map<AstNode, CFG> cfgs;
    //
    // /**
    // * The virtual call stack.
    // */
    // private Stack<StackFrame> callStack;
    //
    // /**
    // * @param cfg
    // * The initial CFG for the function.
    // * @param inAbsState
    // * The incoming abstract state of the system before the function is
    // * executed.
    // */
    // private Analysis(StackFrame initialState, Map<AstNode, CFG> cfgs, StopWatch
    // timer) {
    // this.callStack = new Stack<StackFrame>();
    // this.callStack.push(initialState);
    // this.cfgs = cfgs;
    // this.timer = timer;
    // this.timer.start();
    // this.timer.suspend();
    // }
    //
    // /**
    // * Advance the analysis to the next statement in the program.
    // *
    // * @return the instruction that was last executed
    // */
    // public void run() {
    //
    // State postTransferState;
    //
    // timer.resume();
    //
    // while (!callStack.isEmpty()) {
    // /* Get the next function from the top of the stack. */
    // StackFrame stackFrame = callStack.pop();
    //
    // if (!stackFrame.instrStack.isEmpty()) {
    // /* Get the next instruction from the top of the stack. */
    // BranchInstruction instruction = stackFrame.instrStack.pop();
    //
    // /* Transfer over the edge. */
    // postTransferState = transferEdge(stackFrame, instruction);
    // if (newFrameLoaded) {
    // /* A function was added to the call stack. */
    // newFrameLoaded = false;
    // continue;
    // }
    //
    // /* Transfer over the node. */
    // postTransferState = transferNode(stackFrame, instruction);
    // if (newFrameLoaded) {
    // /* A function was added to the call stack. */
    // newFrameLoaded = false;
    // continue;
    // } else if (postTransferState == null) {
    // /* Wait for other paths to merge before continuing. */
    // if (!stackFrame.isFinished())
    // callStack.push(stackFrame);
    // continue;
    // }
    //
    // /* We are done visiting the edge. */
    // stackFrame.edgesVisited++;
    // }
    //
    // if (!stackFrame.isFinished()) {
    // // We are not at the end of the function, push it back onto the stack.
    // callStack.push(stackFrame);
    // } else {
    // // We have reached the end of the function, so analyze the publicly
    // accessible
    // // functions that weren't analyzed in the main analysis.
    //
    // // TODO: Get the list of stack frames for un-analyzed functions
    // analyzeReachableFunctions(stackFrame);
    // if (frameToLoad != null) {
    // callStack.push(stackFrame);
    // callStack.push(frameToLoad);
    // frameToLoad = null;
    // }
    // }
    //
    // }
    //
    // timer.suspend();
    // }
    //
    // /**
    // * @return {@code true} if the analysis has completed.
    // */
    // public boolean isFinished() {
    // return this.callStack.isEmpty();
    // }
    //
    // /**
    // * Tell the analysis to push the given function call onto the call stack.
    // */
    // public void pushFunctionCall(StackFrame call) {
    // this.frameToLoad = call;
    // }
    //
    // /**
    // * @return a new instance of the analysis.
    // */
    // public static Analysis build(ClassifiedASTNode root, List<CFG> cfgs) {
    //
    // /* Store params. */
    // StopWatch timer = new StopWatch();
    //
    // /* Build a map of AstNodes to CFGs. Used for inter-proc CFA. */
    // Map<AstNode, CFG> cfgMap = new HashMap<AstNode, CFG>();
    // for (CFG cfg : cfgs) {
    // cfg.attachTimer(timer, TIMEOUT);
    // cfgMap.put((AstNode) cfg.getEntryNode().getStatement(), cfg);
    // }
    //
    // /* Setup the analysis with the root script and an initial state. */
    // State state = StateFactory.createInitialState((ScriptNode) root, cfgMap);
    // return new Analysis(new StackFrame(cfgMap.get(root), state), cfgMap, timer);
    //
    // }
    //
    // /**
    // * Analyzes functions which are reachable from the current scope and have not
    // * yet been analyzed.
    // */
    // private void analyzeReachableFunctions(StackFrame stackFrame) {
    // /* Get the set of local vars to search for unanalyzed functions. */
    // Set<String> localVars = new HashSet<String>();
    // List<Name> localVarNames = VariableLiftVisitor
    // .getVariableDeclarations((ScriptNode)
    // stackFrame.cfg.getEntryNode().getStatement());
    // for (Name localVarName : localVarNames)
    // localVars.add(localVarName.toSource());
    //
    // /* Get the set of local functions to search for unanalyzed functions. */
    // List<FunctionNode> localFunctions = FunctionLiftVisitor
    // .getFunctionDeclarations((ScriptNode)
    // stackFrame.cfg.getEntryNode().getStatement());
    // for (FunctionNode localFunction : localFunctions) {
    // Name name = localFunction.getFunctionName();
    // if (name != null)
    // localVars.add(name.toSource());
    // }
    //
    // /* Analyze reachable functions. */
    // State exitState = Helpers.getMergedExitState(stackFrame.cfg);
    // Helpers.analyzeEnvReachable(exitState, exitState.env.environment,
    // exitState.selfAddr, cfgs,
    // new HashSet<Address>(), localVars, this);
    // }
    //
    // /**
    // * Transfer over a CFG node.
    // *
    // * @return The abstract state after the transfer, or null if a function call
    // was
    // * added to the stack.
    // */
    // private State transferNode(StackFrame stackFrame, BranchInstruction
    // instruction) {
    //
    // /*
    // * Join the lattice elements from the current node and 'incoming' edge.
    // */
    // State preTransferState;
    // if (instruction.edge.getTo().getBeforeState() == null)
    // preTransferState = (State) instruction.edge.getAfterState();
    // else
    // preTransferState = ((State) instruction.edge.getAfterState())
    // .join((State) instruction.edge.getTo().getBeforeState());
    // instruction.edge.getTo().setBeforeState(preTransferState);
    //
    // /*
    // * If it does not exist, put it in the map and initialize the semaphore value
    // to
    // * the number of incoming edges for the node.
    // */
    // Integer semVal = stackFrame.iesMap.get(instruction.edge.getTo());
    // if (semVal == null)
    // semVal = instruction.edge.getTo().getIncommingEdgeCount() - 1;
    //
    // /*
    // * Transfer the abstract state over the node, only if we need to make progress
    // * in the analysis by visiting a downstream edge.
    // */
    // if (!Analysis.doTransfer(instruction.edge.getTo(), semVal))
    // return null;
    //
    // /* Transfer the abstract state over the edge. */
    // State postTransferState;
    // postTransferState = preTransferState.clone();
    //
    // TransferNode transferFunction;
    // transferFunction = new TransferNode(postTransferState,
    // instruction.edge.getTo(),
    // evaluator(postTransferState));
    // transferFunction.transfer();
    //
    // /*
    // * If the transfer function requested a new call be pushed onto the stack, we
    // * need to (1) 'pause' this analysis (by pushing this instruction back onto
    // the
    // * stack), and (2) 'call' the new function (by push the new AnalysisState onto
    // * the call stack)
    // */
    // if (this.frameToLoad != null) {
    // stackFrame.instrStack.push(instruction);
    // this.callStack.push(stackFrame);
    // this.callStack.push(this.frameToLoad);
    // this.frameToLoad = null;
    // this.newFrameLoaded = true;
    // return null;
    // }
    //
    // instruction.edge.getTo().setAfterState(postTransferState);
    //
    // /*
    // * Add all unvisited edges to the stack. We currently only execute loops once.
    // */
    // for (CFGEdge edge : instruction.edge.getTo().getOutgoingEdges()) {
    //
    // /*
    // * Only visit an edge if the semaphore for the current node is zero or if one
    // of
    // * the edges is a loop edge.
    // */
    // if (!instruction.visited.contains(edge) && (semVal == 0 || edge.isLoopEdge))
    // {
    // Set<CFGEdge> newVisited = new HashSet<CFGEdge>(instruction.visited);
    // newVisited.add(edge);
    // BranchInstruction nextInstruction = new BranchInstruction(edge, newVisited);
    // stackFrame.instrStack.push(nextInstruction);
    // }
    //
    // }
    //
    // /* Update the state of progress to node complete. */
    // stackFrame.completeNode();
    //
    // return postTransferState;
    //
    // }
    //
    // /**
    // * Transfer over a CFG edge.
    // *
    // * @return The abstract state after the transfer, or null if a function call
    // was
    // * added to the stack.
    // */
    // private State transferEdge(StackFrame stackFrame, BranchInstruction
    // instruction) {
    //
    // /*
    // * Join the lattice elements from the current edge and 'from' node.
    // */
    // State preTransferState;
    // preTransferState = ((State) instruction.edge.getFrom().getAfterState())
    // .join((State) instruction.edge.getBeforeState());
    // instruction.edge.setBeforeState(preTransferState);
    //
    // /* Transfer the abstract state over the edge. */
    // State postTransferState;
    // postTransferState = preTransferState.clone();
    //
    // TransferEdge transferFunction;
    // transferFunction = new TransferEdge(postTransferState, instruction.edge,
    // evaluator(postTransferState));
    // transferFunction.transfer();
    //
    // /*
    // * If the transfer function requested a new call be pushed onto the stack, we
    // * need to (1) 'pause' this analysis (by pushing this instruction back onto
    // the
    // * stack), and (2) 'call' the new function (by push the new AnalysisState onto
    // * the call stack)
    // */
    // if (this.frameToLoad != null) {
    // stackFrame.instrStack.push(instruction);
    // this.callStack.push(stackFrame);
    // this.callStack.push(this.frameToLoad);
    // this.frameToLoad = null;
    // this.newFrameLoaded = true;
    // return null;
    // }
    //
    // /* Join with the old abstract state. */
    // instruction.edge
    // .setAfterState(postTransferState.join((State)
    // instruction.edge.getAfterState()));
    //
    // /*
    // * Look up the number of times this node has been visited in the
    // * visitedSemaphores map.
    // */
    // Integer semVal = stackFrame.iesMap.get(instruction.edge.getTo());
    //
    // /*
    // * If it does not exist, put it in the map and initialize the semaphore value
    // to
    // * the number of incoming edges for the node.
    // */
    // if (semVal == null)
    // semVal = instruction.edge.getTo().getIncommingEdgeCount();
    //
    // /* Decrement the semaphore by one since we visited the node. */
    // semVal = semVal - 1;
    // stackFrame.iesMap.put(instruction.edge.getTo(), semVal);
    //
    // /* Update the state of progress to edge complete. */
    // stackFrame.completeEdge();
    //
    // return postTransferState;
    //
    // }
    //
    // /**
    // * @return an evaluator for the given state.
    // */
    // public ExpEval evaluator(State state) {
    // return new ExpEval(this, state);
    // }
    //
    // /**
    // * Decide whether or not to transfer over a node. We transfer if (a) all
    // * incoming edges have been visited (semVal == 0) or if (b) there is an
    // * unvisited loop edge. If semVal == -1, do not proceed because a call has
    // been
    // * added to the call stack and must first be analyzed.
    // *
    // * @param pathState
    // * The current state of the path.
    // * @param semVal
    // * The number of incoming edges that have not yet been visited.
    // * @return {@code true} if we need to transfer over the node.
    // */
    // public static boolean doTransfer(CFGNode node, int semVal) {
    // if (semVal == 0)
    // return true;
    // for (CFGEdge edge : node.getOutgoingEdges())
    // if (edge.isLoopEdge)
    // return true;
    // return false;
    // }

}
