package org.drools.reteoo;

/*
 * Copyright 2005 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.LinkedList;

import org.drools.RuleBaseConfiguration;
import org.drools.common.BaseNode;
import org.drools.common.InternalWorkingMemory;
import org.drools.common.NodeMemory;
import org.drools.common.PropagationContextImpl;
import org.drools.rule.GroupElement;
import org.drools.rule.Rule;
import org.drools.spi.PropagationContext;

/**
 * Leaf Rete-OO node responsible for enacting <code>Action</code> s on a
 * matched <code>Rule</code>.
 * 
 * @see org.drools.rule.Rule
 * 
 * @author <a href="mailto:bob@eng.werken.com">bob mcwhirter </a>
 */
public final class QueryTerminalNode extends BaseNode
    implements
    TupleSinkNode,
    NodeMemory,
    TerminalNode {
    // ------------------------------------------------------------
    // Instance members
    // ------------------------------------------------------------

    /**
     * 
     */
    private static final long  serialVersionUID = 400L;
    /** The rule to invoke upon match. */
    private final Rule         rule;
    private final GroupElement subrule;
    private final TupleSource  tupleSource;
    private boolean          tupleMemoryEnabled;    
    
    private TupleSinkNode      previousTupleSinkNode;
    private TupleSinkNode      nextTupleSinkNode;    

    // ------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------

    /**
     * Construct.
     * 
     * @param inputSource
     *            The parent tuple source.
     * @param rule
     *            The rule.
     */
    public QueryTerminalNode(final int id,
                             final TupleSource source,
                             final Rule rule,
                             final GroupElement subrule) {
        super( id );
        this.rule = rule;
        this.subrule = subrule;
        this.tupleSource = source;
        this.tupleMemoryEnabled = false; //hard coded to false
    }

    // ------------------------------------------------------------
    // Instance methods
    // ------------------------------------------------------------

    /**
     * Retrieve the <code>Action</code> associated with this node.
     * 
     * @return The <code>Action</code> associated with this node.
     */
    public Rule getRule() {
        return this.rule;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // org.drools.impl.TupleSink
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Assert a new <code>Tuple</code>.
     * 
     * @param tuple
     *            The <code>Tuple</code> being asserted.
     * @param workingMemory
     *            The working memory seesion.
     * @throws AssertionException
     *             If an error occurs while asserting.
     */
    public void assertTuple(final ReteTuple tuple,
                            final PropagationContext context,
                            final InternalWorkingMemory workingMemory) {
        final LinkedList list = (LinkedList) workingMemory.getNodeMemory( this );
        if ( list.isEmpty() ) {
            ((ReteooWorkingMemory) workingMemory).setQueryResults( this.rule.getName(),
                                                                   this );
        }
        list.add( tuple );
    }

    public void retractTuple(final ReteTuple tuple,
                             final PropagationContext context,
                             final InternalWorkingMemory workingMemory) {
    }

    public String toString() {
        return "[QueryTerminalNode: rule=" + this.rule.getName() + "]";
    }

    public void ruleAttached() {

    }

    public void attach() {
        this.tupleSource.addTupleSink( this );
    }

    public void attach(final InternalWorkingMemory[] workingMemories) {
        attach();

        for ( int i = 0, length = workingMemories.length; i < length; i++ ) {
            final InternalWorkingMemory workingMemory = workingMemories[i];
            final PropagationContext propagationContext = new PropagationContextImpl( workingMemory.getNextPropagationIdCounter(),
                                                                                      PropagationContext.RULE_ADDITION,
                                                                                      null,
                                                                                      null );
            this.tupleSource.updateSink( this,
                                         propagationContext,
                                         workingMemory );
        }
    }

    public void remove(final BaseNode node,
                       final InternalWorkingMemory[] workingMemories) {
        for ( int i = 0, length = workingMemories.length; i < length; i++ ) {
            workingMemories[i].clearNodeMemory( this );
        }
        
        removeShare();
        
        this.tupleSource.remove( this,
                                 workingMemories );
    }

    public void updateNewNode(final InternalWorkingMemory workingMemory,
                              final PropagationContext context) {
        // There are no child nodes to update, do nothing.
    }

    public Object createMemory(final RuleBaseConfiguration config) {
        return new LinkedList();
    }

    public boolean isTupleMemoryEnabled() {
        return tupleMemoryEnabled;
    }

    public void setTupleMemoryEnabled(boolean tupleMemoryEnabled) {
        this.tupleMemoryEnabled = tupleMemoryEnabled;
    }
    
    /**
     * @return the subrule
     */
    public GroupElement getSubrule() {
        return this.subrule;
    }
    
    /**
     * Returns the previous node
     * @return
     *      The previous TupleSinkNode
     */
    public TupleSinkNode getPreviousTupleSinkNode() {
        return this.previousTupleSinkNode;
    }

    /**
     * Sets the previous node 
     * @param previous
     *      The previous TupleSinkNode
     */
    public void setPreviousTupleSinkNode(final TupleSinkNode previous) {
        this.previousTupleSinkNode = previous;
    }    
    
    /**
     * Returns the next node
     * @return
     *      The next TupleSinkNode
     */
    public TupleSinkNode getNextTupleSinkNode() {
        return this.nextTupleSinkNode;
    }

    /**
     * Sets the next node 
     * @param next
     *      The next TupleSinkNode
     */
    public void setNextTupleSinkNode(final TupleSinkNode next) {
        this.nextTupleSinkNode = next;
    }    

}
