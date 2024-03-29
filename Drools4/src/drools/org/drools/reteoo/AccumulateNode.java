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

package org.drools.reteoo;

import java.util.Arrays;

import org.drools.RuleBaseConfiguration;
import org.drools.RuntimeDroolsException;
import org.drools.common.BetaConstraints;
import org.drools.common.InternalFactHandle;
import org.drools.common.InternalWorkingMemory;
import org.drools.reteoo.builder.BuildContext;
import org.drools.rule.Accumulate;
import org.drools.spi.AlphaNodeFieldConstraint;
import org.drools.spi.PropagationContext;
import org.drools.util.ArrayUtils;
import org.drools.util.Entry;
import org.drools.util.FactEntry;
import org.drools.util.Iterator;
import org.drools.util.ObjectHashMap.ObjectEntry;

/**
 * AccumulateNode
 * A beta node capable of doing accumulate logic.
 *
 * Created: 04/06/2006
 * @author <a href="mailto:tirelli@post.com">Edson Tirelli</a> 
 *
 * @version $Id: AccumulateNode.java 15266 2007-09-20 20:15:33Z tirelli $
 */
public class AccumulateNode extends BetaNode {

    private static final long                serialVersionUID = 400L;

    private final boolean                    unwrapRightObject;
    private final Accumulate                 accumulate;
    private final AlphaNodeFieldConstraint[] resultConstraints;
    private final BetaConstraints            resultBinder;

    public AccumulateNode(final int id,
                          final TupleSource leftInput,
                          final ObjectSource rightInput,
                          final AlphaNodeFieldConstraint[] resultConstraints,
                          final BetaConstraints sourceBinder,
                          final BetaConstraints resultBinder,
                          final Accumulate accumulate,
                          final boolean unwrapRightObject,
                          final BuildContext context) {
        super( id,
               leftInput,
               rightInput,
               sourceBinder );
        this.resultBinder = resultBinder;
        this.resultConstraints = resultConstraints;
        this.accumulate = accumulate;
        this.unwrapRightObject = unwrapRightObject;
        this.tupleMemoryEnabled = context.isTupleMemoryEnabled();
    }

    /**
     * @inheritDoc
     * 
     *  When a new tuple is asserted into an AccumulateNode, do this:
     *  
     *  1. Select all matching objects from right memory
     *  2. Execute the initialization code using the tuple + matching objects
     *  3. Execute the accumulation code for each combination of tuple+object
     *  4. Execute the return code
     *  5. Create a new CalculatedObjectHandle for the resulting object and add it to the tuple
     *  6. Propagate the tuple
     *  
     *  The initialization, accumulation and return codes, in JBRules, are assembled
     *  into a generated method code and called once for the whole match, as you can see
     *  bellow:
     *  
     *   Object result = this.accumulator.accumulate( ... );
     *  
     */
    public void assertTuple(final ReteTuple leftTuple,
                            final PropagationContext context,
                            final InternalWorkingMemory workingMemory) {

        final AccumulateMemory memory = (AccumulateMemory) workingMemory.getNodeMemory( this );

        AccumulateResult accresult = new AccumulateResult();

        if ( this.tupleMemoryEnabled ) {
            memory.betaMemory.getTupleMemory().add( leftTuple );
            memory.betaMemory.getCreatedHandles().put( leftTuple,
                                            accresult,
                                            false );
        }

        final Object accContext = this.accumulate.createContext();

        accresult.context = accContext;
        this.accumulate.init( memory.workingMemoryContext,
                              accContext,
                              leftTuple,
                              workingMemory );

        final Iterator it = memory.betaMemory.getFactHandleMemory().iterator( leftTuple );
        this.constraints.updateFromTuple( workingMemory,
                                          leftTuple );

        for ( FactEntry entry = (FactEntry) it.next(); entry != null; entry = (FactEntry) it.next() ) {
            InternalFactHandle handle = entry.getFactHandle();
            if ( this.constraints.isAllowedCachedLeft( handle.getObject() ) ) {
                if ( this.unwrapRightObject ) {
                    // if there is a subnetwork, handle must be unwrapped
                    ReteTuple tuple = (ReteTuple) handle.getObject(); 
                    handle = tuple.getLastHandle();
                    this.accumulate.accumulate( memory.workingMemoryContext,
                                                accContext,
                                                tuple,
                                                handle,
                                                workingMemory );
                } else {
                    this.accumulate.accumulate( memory.workingMemoryContext,
                                                accContext,
                                                leftTuple,
                                                handle,
                                                workingMemory );
                }
            }
        }

        final Object result = this.accumulate.getResult( memory.workingMemoryContext,
                                                         accContext,
                                                         leftTuple,
                                                         workingMemory );
        
        if( result == null ) {
            throw new RuntimeDroolsException("Accumulate must not return a null value.");
        }

        // First alpha node filters
        boolean isAllowed = true;
        for ( int i = 0, length = this.resultConstraints.length; i < length; i++ ) {
            if ( !this.resultConstraints[i].isAllowed( result,
                                                       workingMemory ) ) {
                isAllowed = false;
                break;
            }
        }
        if ( isAllowed ) {
            this.resultBinder.updateFromTuple( workingMemory,
                                               leftTuple );
            if ( this.resultBinder.isAllowedCachedLeft( result ) ) {
                final InternalFactHandle handle = workingMemory.getFactHandleFactory().newFactHandle( result );
                accresult.handle = handle;

                this.sink.propagateAssertTuple( leftTuple,
                                                handle,
                                                context,
                                                workingMemory );
            }
        }

    }

    /**
     * @inheritDoc
     * 
     * As the accumulate node will always propagate the tuple,
     * it must always also retreat it.
     * 
     */
    public void retractTuple(final ReteTuple leftTuple,
                             final PropagationContext context,
                             final InternalWorkingMemory workingMemory) {
        final AccumulateMemory memory = (AccumulateMemory) workingMemory.getNodeMemory( this );
        memory.betaMemory.getTupleMemory().remove( leftTuple );
        final AccumulateResult accresult = (AccumulateResult) memory.betaMemory.getCreatedHandles().remove( leftTuple );

        // if tuple was propagated
        if ( accresult.handle != null ) {
            this.sink.propagateRetractTuple( leftTuple,
                                             accresult.handle,
                                             context,
                                             workingMemory );

            // Destroying the acumulate result object 
            workingMemory.getFactHandleFactory().destroyFactHandle( accresult.handle );
        }

    }

    /**
     * @inheritDoc
     * 
     *  When a new object is asserted into an AccumulateNode, do this:
     *  
     *  1. Select all matching tuples from left memory
     *  2. For each matching tuple, call a modify tuple
     *  
     */
    public void assertObject(final InternalFactHandle handle,
                             final PropagationContext context,
                             final InternalWorkingMemory workingMemory) {

        final AccumulateMemory memory = (AccumulateMemory) workingMemory.getNodeMemory( this );
        memory.betaMemory.getFactHandleMemory().add( handle );

        if ( ! this.tupleMemoryEnabled ) {
            // do nothing here, as we know there are no left tuples at this stage in sequential mode.
            return;
        }

        this.constraints.updateFromFactHandle( workingMemory,
                                               handle );

        // need to clone the tuples to avoid concurrent modification exceptions
        Entry[] tuples = memory.betaMemory.getTupleMemory().toArray();
        for ( int i = 0; i < tuples.length; i++ ) {
            ReteTuple tuple = (ReteTuple) tuples[i];
            if ( this.constraints.isAllowedCachedRight( tuple ) ) {
                if ( this.accumulate.supportsReverse() || context.getType() == PropagationContext.ASSERTION ) {
                    modifyTuple( true,
                                 tuple,
                                 handle,
                                 context,
                                 workingMemory );
                } else {
                    // context is MODIFICATION and does not supports reverse
                    this.retractTuple( tuple,
                                       context,
                                       workingMemory );
                    this.assertTuple( tuple,
                                      context,
                                      workingMemory );
                }
            }
        }
    }

    /**
     *  @inheritDoc
     *  
     *  If an object is retract, call modify tuple for each
     *  tuple match.
     */
    public void retractObject(final InternalFactHandle handle,
                              final PropagationContext context,
                              final InternalWorkingMemory workingMemory) {
        final AccumulateMemory memory = (AccumulateMemory) workingMemory.getNodeMemory( this );
        if ( !memory.betaMemory.getFactHandleMemory().remove( handle ) ) {
            return;
        }

        this.constraints.updateFromFactHandle( workingMemory,
                                               handle );
        // need to clone the tuples to avoid concurrent modification exceptions
        Entry[] tuples = memory.betaMemory.getTupleMemory().toArray();
        for ( int i = 0; i < tuples.length; i++ ) {
            ReteTuple tuple = (ReteTuple) tuples[i];
            if ( this.constraints.isAllowedCachedRight( tuple ) ) {
                if ( this.accumulate.supportsReverse() ) {
                    this.modifyTuple( false,
                                      tuple,
                                      handle,
                                      context,
                                      workingMemory );
                } else {
                    this.retractTuple( tuple,
                                       context,
                                       workingMemory );
                    this.assertTuple( tuple,
                                      context,
                                      workingMemory );
                }
            }
        }
    }

    public void modifyTuple(final boolean isAssert,
                            final ReteTuple leftTuple,
                            InternalFactHandle handle,
                            final PropagationContext context,
                            final InternalWorkingMemory workingMemory) {

        final AccumulateMemory memory = (AccumulateMemory) workingMemory.getNodeMemory( this );
        AccumulateResult accresult = (AccumulateResult) memory.betaMemory.getCreatedHandles().get( leftTuple );

        // if tuple was propagated
        if ( accresult.handle != null ) {
            this.sink.propagateRetractTuple( leftTuple,
                                             accresult.handle,
                                             context,
                                             workingMemory );

            // Destroying the acumulate result object 
            workingMemory.getFactHandleFactory().destroyFactHandle( accresult.handle );
            accresult.handle = null;
        }

        ReteTuple tuple = leftTuple;
        if ( this.unwrapRightObject ) {
            // if there is a subnetwork, handle must be unwrapped
            tuple = (ReteTuple) handle.getObject();
            handle = tuple.getLastHandle();
        }

        if ( context.getType() == PropagationContext.ASSERTION ) {
            // assertion
            if ( accresult.context == null ) {
                final Object accContext = this.accumulate.createContext();

                this.accumulate.init( memory.workingMemoryContext,
                                      accContext,
                                      leftTuple,
                                      workingMemory );

                accresult.context = accContext;
            }

            this.accumulate.accumulate( memory.workingMemoryContext,
                                        accresult.context,
                                        tuple,
                                        handle,
                                        workingMemory );
        } else if ( context.getType() == PropagationContext.MODIFICATION ) {
            // modification
            if ( isAssert ) {
                this.accumulate.accumulate( memory.workingMemoryContext,
                                            accresult.context,
                                            tuple,
                                            handle,
                                            workingMemory );
            } else {
                this.accumulate.reverse( memory.workingMemoryContext,
                                         accresult.context,
                                         tuple,
                                         handle,
                                         workingMemory );
            }
        } else {
            // retraction
            this.accumulate.reverse( memory.workingMemoryContext,
                                     accresult.context,
                                     tuple,
                                     handle,
                                     workingMemory );
        }

        final Object result = this.accumulate.getResult( memory.workingMemoryContext,
                                                         accresult.context,
                                                         leftTuple,
                                                         workingMemory );

        if( result == null ) {
            throw new RuntimeDroolsException("Accumulate must not return a null value.");
        }

        // First alpha node filters
        boolean isAllowed = true;
        for ( int i = 0, length = this.resultConstraints.length; i < length; i++ ) {
            if ( !this.resultConstraints[i].isAllowed( result,
                                                       workingMemory ) ) {
                isAllowed = false;
                break;
            }
        }
        if ( isAllowed ) {
            this.resultBinder.updateFromTuple( workingMemory,
                                               leftTuple );
            if ( this.resultBinder.isAllowedCachedLeft( result ) ) {
                final InternalFactHandle createdHandle = workingMemory.getFactHandleFactory().newFactHandle( result );
                accresult.handle = createdHandle;

                this.sink.propagateAssertTuple( leftTuple,
                                                createdHandle,
                                                context,
                                                workingMemory );
            }
        }
    }

    public void updateSink(final TupleSink sink,
                           final PropagationContext context,
                           final InternalWorkingMemory workingMemory) {
        final AccumulateMemory memory = (AccumulateMemory) workingMemory.getNodeMemory( this );

        final Iterator it = memory.betaMemory.getCreatedHandles().iterator();

        for ( ObjectEntry entry = (ObjectEntry) it.next(); entry != null; entry = (ObjectEntry) it.next() ) {
            AccumulateResult accresult = (AccumulateResult) entry.getValue();
            sink.assertTuple( new ReteTuple( (ReteTuple) entry.getKey(),
                                             accresult.handle ),
                              context,
                              workingMemory );
        }
    }

    /* (non-Javadoc)
     * @see org.drools.reteoo.BaseNode#hashCode()
     */
    public int hashCode() {
        return this.leftInput.hashCode() ^ this.rightInput.hashCode() ^ this.accumulate.hashCode() ^ this.resultBinder.hashCode() ^ ArrayUtils.hashCode( this.resultConstraints );
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(final Object object) {
        if ( this == object ) {
            return true;
        }

        if ( object == null || !(object instanceof AccumulateNode) ) {
            return false;
        }

        final AccumulateNode other = (AccumulateNode) object;

        if ( this.getClass() != other.getClass() || (!this.leftInput.equals( other.leftInput )) || (!this.rightInput.equals( other.rightInput )) || (!this.constraints.equals( other.constraints )) ) {
            return false;
        }

        return this.accumulate.equals( other.accumulate ) && resultBinder.equals( other.resultBinder ) && Arrays.equals( this.resultConstraints,
                                                                                                                         other.resultConstraints );
    }

    public String toString() {
        return "[ " + this.getClass().getName() + "(" + this.id + ") ]";
    }

    /**
     * Creates a BetaMemory for the BetaNode's memory.
     */
    public Object createMemory(final RuleBaseConfiguration config) {
        AccumulateMemory memory = new AccumulateMemory();
        memory.betaMemory = this.constraints.createBetaMemory( config );
        memory.workingMemoryContext = this.accumulate.createWorkingMemoryContext();
        return memory;
    }

    public static class AccumulateMemory {
        private static final long serialVersionUID = 400L;
        
        public Object workingMemoryContext;
        public BetaMemory betaMemory;
    }

    private static class AccumulateResult {
        // keeping attributes public just for performance
        public InternalFactHandle handle;
        public Object             context;
    }      
}
