package multidiffplus.cfg;

import java.util.LinkedList;
import java.util.List;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;

/**
 * A control flow graph node and abstract state for flow analysis.
 */
public class CFGNode {

    /** The unique id for this node. **/
    private int id;

    /** Optional name for this node. **/
    private String name;

    /**
     * The AST Statement which contains the actions this node performs. From
     * org.mozilla.javascript.Token.
     **/
    private ClassifiedASTNode statement;

    /** The edges entering this node. **/
    private List<CFGEdge> inEdges;

    /** The edges leaving this node. **/
    private List<CFGEdge> outEdges;

    /** The corresponding source or destination CFGNode. */
    private CFGNode mappedNode;

    /**
     * The state of the environment and store before transferring over the term
     * (statement). The state is language dependent.
     */
    private IState beforeState;

    /**
     * The state of the environment and store after transferring over the term
     * (statement). The state is language dependent.
     */
    private IState afterState;

    /**
     * @param statement
     *            The statement that is executed when this node is reached.
     */
    public CFGNode(ClassifiedASTNode statement, int id) {
	this.inEdges = new LinkedList<CFGEdge>();
	this.outEdges = new LinkedList<CFGEdge>();
	this.statement = statement;
	this.id = id;
	this.name = null;
	this.setMappedNode(null);
	this.beforeState = null;
	this.afterState = null;
    }

    /**
     * @param statement
     *            The statement that is executed when this node is reached.
     * @param name
     *            The name for this node (nice for printing and debugging).
     */
    public CFGNode(ClassifiedASTNode statement, String name, int id) {
	this.inEdges = new LinkedList<CFGEdge>();
	this.outEdges = new LinkedList<CFGEdge>();
	this.statement = statement;
	this.id = id;
	this.name = name;
	this.beforeState = null;
	this.afterState = null;
    }

    /**
     * Accepts and runs a visitor.
     */
    public void accept(ICFGVisitor visitor) {
	visitor.visit(this);
    }

    /**
     * Set the lattice element at this point in the program.
     * 
     * @param as
     *            The abstract state.
     */
    public void setBeforeState(IState state) {
	this.beforeState = state;
    }

    /**
     * @return the abstract state at this point in the program.
     */
    public IState getBeforeState() {
	return this.beforeState;
    }

    /**
     * Set the lattice element at this point in the program.
     * 
     * @param as
     *            The abstract state.
     */
    public void setAfterState(IState state) {
	this.afterState = state;
    }

    /**
     * @return the abstract state at this point in the program.
     */
    public IState getAfterState() {
	return this.afterState;
    }

    /**
     * Increments the number of edges that point to this node by one.
     */
    public void addIncommingEdge(CFGEdge inEdge) {
	this.inEdges.add(inEdge);
    }

    /**
     * @return The edges comming into this node.
     */
    public List<CFGEdge> getIncommingEdges() {
	return this.inEdges;
    }

    /**
     * @return The number of edges that point to this node.
     */
    public int getIncommingEdgeCount() {
	return this.inEdges.size();
    }

    /**
     * @return true if this node already contains an edge with this condition.
     */
    private CFGEdge getOutgoingEdge(ClassifiedASTNode condition) {
	for (CFGEdge existing : this.outEdges) {
	    if (condition == existing.getCondition())
		return existing;
	}
	return null;
    }

    /**
     * Add an edge to this node. If an edge with the same condition already exists,
     * that edge will be overwritten.
     * 
     * @param condition
     *            The condition for which we traverse the edge.
     * @param node
     *            The node at the other end of this edge.
     */
    public void addOutgoingEdge(ClassifiedASTNode condition, CFGNode node, int id) {
	CFGEdge edge = this.getOutgoingEdge(condition);

	if (edge != null) {
	    edge.setTo(node);
	} else {
	    edge = new CFGEdge(condition, this, node, id);
	    this.outEdges.add(new CFGEdge(condition, this, node, id));
	}
    }

    /**
     * Add an edge to this node. If an edge with the same condition already exists,
     * that edge will be overwritten.
     * 
     * @param condition
     *            The condition for which we traverse the edge.
     * @param node
     *            The node at the other end of this edge.
     */
    public void addOutgoingEdge(ClassifiedASTNode condition, CFGNode node, boolean loopEdge,
	    int id) {
	CFGEdge edge = this.getOutgoingEdge(condition);

	if (edge != null) {
	    edge.setTo(node);
	} else {
	    edge = new CFGEdge(condition, this, node, id);
	    this.outEdges.add(new CFGEdge(condition, this, node, loopEdge, id));
	}
    }

    /**
     * Add an edge to this node. If an edge with the same condition already exists,
     * that edge will be overwritten.
     * 
     * @param edge
     *            The edge to add.
     */
    public void addOutgoingEdge(CFGEdge edge) {
	CFGEdge existing = this.getOutgoingEdge(edge.getCondition());

	if (existing != null) {
	    existing.setTo(edge.getTo());
	} else {
	    this.outEdges.add(edge);
	}
    }

    /**
     * @return The nodes pointed to by this node.
     */
    public List<CFGNode> getAdjacentNodes() {
	List<CFGNode> nodes = new LinkedList<CFGNode>();
	for (CFGEdge edge : this.outEdges)
	    nodes.add(edge.getTo());
	return nodes;
    }

    /**
     * @return The edges leaving this node.
     */
    public List<CFGEdge> getOutgoingEdges() {
	return this.outEdges;
    }

    /**
     * @param edges
     *            The new edges for the node.
     */
    public void setEdges(List<CFGEdge> edges) {
	this.outEdges = edges;
    }

    /**
     * @return The AST Statement which contains the actions this node performs.
     */
    public ClassifiedASTNode getStatement() {
	return statement;
    }

    /**
     * @param statement
     *            The AST Statement which contains the actions this node performs.
     */
    public void setStatement(ClassifiedASTNode statement) {
	this.statement = statement;
    }

    /**
     * @return The unique ID for this node.
     */
    public int getId() {
	return id;
    }

    /**
     * @return the corresponding node in the source or destination CFG.
     */
    public CFGNode getMappedNode() {
	return mappedNode;
    }

    /**
     * @param mappedNode
     *            the corresponding node in the source or destination CFG.
     */
    public void setMappedNode(CFGNode mappedNode) {
	this.mappedNode = mappedNode;
    }

    public String getName() {

	if (this.name != null)
	    return this.name;

	return this.statement.getASTNodeType();

    }

    /**
     * Make a copy of the given node.
     * 
     * @param node
     *            The node to copy.
     * @return A shallow copy of the node. The condition AST and edge CFGs will be
     *         the same as the original.
     */
    public static CFGNode copy(CFGNode node) {
	return copy(node, node.getId());
    }

    /**
     * Make a copy of the given node and assign it the given Id.
     * 
     * @param node
     *            The node to copy.
     * @param id
     *            The Id for the new node.
     * @return A shallow copy of the node. The condition AST and edge CFGs will be
     *         the same as the original.
     */
    public static CFGNode copy(CFGNode node, int id) {
	CFGNode newNode = new CFGNode(node.getStatement(), id);
	for (CFGEdge edge : node.getOutgoingEdges())
	    newNode.addOutgoingEdge(edge.getCondition(), edge.getTo(), edge.getId());
	return newNode;
    }

    @Override
    public String toString() {
	return this.id + " = " + this.getStatement().toString().replaceAll("\n", ": ");
    }

}
