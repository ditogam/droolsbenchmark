package org.drools.common;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.drools.RuleBaseConfiguration;
import org.drools.base.evaluators.Operator;
import org.drools.reteoo.BetaMemory;
import org.drools.reteoo.FactHandleMemory;
import org.drools.reteoo.ReteTuple;
import org.drools.reteoo.TupleMemory;
import org.drools.rule.ContextEntry;
import org.drools.rule.VariableConstraint;
import org.drools.spi.BetaNodeFieldConstraint;
import org.drools.util.FactHashTable;
import org.drools.util.FactHandleIndexHashTable;
import org.drools.util.FactList;
import org.drools.util.LinkedList;
import org.drools.util.LinkedListEntry;
import org.drools.util.TupleHashTable;
import org.drools.util.TupleIndexHashTable;
import org.drools.util.AbstractHashTable.FieldIndex;

public class TripleBetaConstraints
    implements
    Serializable,
    BetaConstraints {

    /**
     * 
     */
    private static final long             serialVersionUID = 400L;

    private final BetaNodeFieldConstraint constraint0;
    private final BetaNodeFieldConstraint constraint1;
    private final BetaNodeFieldConstraint constraint2;

    private final ContextEntry            context0;
    private final ContextEntry            context1;
    private final ContextEntry            context2;

    private boolean                       indexed0;
    private boolean                       indexed1;
    private boolean                       indexed2;

    public TripleBetaConstraints(final BetaNodeFieldConstraint[] constraints,
                                 final RuleBaseConfiguration conf) {
        this( constraints,
              conf,
              false );
    }

    public TripleBetaConstraints(final BetaNodeFieldConstraint[] constraints,
                                 final RuleBaseConfiguration conf,
                                 final boolean disableIndexing) {
        if ( disableIndexing || (!conf.isIndexLeftBetaMemory() && !conf.isIndexRightBetaMemory()) ) {
            this.indexed0 = false;
            this.indexed1 = false;
            this.indexed2 = false;
        } else {
            final int depth = conf.getCompositeKeyDepth();

            // Determine  if this constraints are indexable               
            final boolean i0 = isIndexable( constraints[0] );
            final boolean i1 = isIndexable( constraints[1] );
            final boolean i2 = isIndexable( constraints[2] );

            if ( depth >= 1 && i0 ) {
                this.indexed0 = true;
            }

            if ( i1 ) {
                if ( depth >= 1 && !this.indexed0 ) {
                    this.indexed0 = true;
                    swap( constraints,
                          1,
                          0 );
                } else if ( depth >= 2 ) {
                    this.indexed1 = true;
                }
            }

            if ( i2 ) {
                if ( depth >= 1 && !this.indexed0 ) {
                    this.indexed0 = true;
                    swap( constraints,
                          2,
                          0 );
                } else if ( depth >= 2 && this.indexed0 && !this.indexed1 ) {
                    this.indexed1 = true;
                    swap( constraints,
                          2,
                          1 );
                } else if ( depth >= 3 ) {
                    this.indexed2 = true;
                }
            }
        }
        this.constraint0 = constraints[0];
        this.context0 = this.constraint0.getContextEntry();

        this.constraint1 = constraints[1];
        this.context1 = this.constraint1.getContextEntry();

        this.constraint2 = constraints[2];
        this.context2 = this.constraint2.getContextEntry();
    }

    private void swap(final BetaNodeFieldConstraint[] constraints,
                      final int p1,
                      final int p2) {
        final BetaNodeFieldConstraint temp = constraints[p2];
        constraints[p2] = constraints[p1];
        constraints[p1] = temp;
    }

    private boolean isIndexable(final BetaNodeFieldConstraint constraint) {
        if ( constraint instanceof VariableConstraint ) {
            final VariableConstraint variableConstraint = (VariableConstraint) constraint;
            return (variableConstraint.getEvaluator().getOperator() == Operator.EQUAL);
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.drools.common.BetaNodeConstraints#updateFromTuple(org.drools.reteoo.ReteTuple)
     */
    public void updateFromTuple(final InternalWorkingMemory workingMemory,
                                final ReteTuple tuple) {
        this.context0.updateFromTuple( workingMemory,
                                       tuple );
        this.context1.updateFromTuple( workingMemory,
                                       tuple );
        this.context2.updateFromTuple( workingMemory,
                                       tuple );
    }

    /* (non-Javadoc)
     * @see org.drools.common.BetaNodeConstraints#updateFromFactHandle(org.drools.common.InternalFactHandle)
     */
    public void updateFromFactHandle(final InternalWorkingMemory workingMemory,
                                     final InternalFactHandle handle) {
        this.context0.updateFromFactHandle( workingMemory,
                                            handle );
        this.context1.updateFromFactHandle( workingMemory,
                                            handle );
        this.context2.updateFromFactHandle( workingMemory,
                                            handle );
    }

    /* (non-Javadoc)
     * @see org.drools.common.BetaNodeConstraints#isAllowedCachedLeft(java.lang.Object)
     */
    public boolean isAllowedCachedLeft(final Object object) {
        //        return ( this.indexed0 || this.constraint0.isAllowedCachedLeft( context0,
        //                                                                       object ) ) && this.constraint1.isAllowedCachedLeft( context1,
        //                                                                                                       object ) && this.constraint2.isAllowedCachedLeft( context2,
        //                                                                                                                                                         object );

        return (this.indexed0 || this.constraint0.isAllowedCachedLeft( this.context0,
                                                                       object )) && (this.indexed1 || this.constraint1.isAllowedCachedLeft( this.context1,
                                                                                                                                            object )) && (this.indexed2 || this.constraint2.isAllowedCachedLeft( this.context2,
                                                                                                                                                                                                                 object ));
    }

    /* (non-Javadoc)
     * @see org.drools.common.BetaNodeConstraints#isAllowedCachedRight(org.drools.reteoo.ReteTuple)
     */
    public boolean isAllowedCachedRight(final ReteTuple tuple) {
        return this.constraint0.isAllowedCachedRight( tuple,
                                                      this.context0 ) && this.constraint1.isAllowedCachedRight( tuple,
                                                                                                                this.context1 ) && this.constraint2.isAllowedCachedRight( tuple,
                                                                                                                                                                          this.context2 );
    }

    public boolean isIndexed() {
        return this.indexed0;
    }
    
    public int getIndexCount() {
        int count = 0;
        if ( this.indexed0 ) {
            count++;
        }
        if ( this.indexed1 ) {
            count++;
        }
        if ( this.indexed2 ) {
            count++;
        }   
        return count;    
    }  

    public boolean isEmpty() {
        return false;
    }

    public BetaMemory createBetaMemory(final RuleBaseConfiguration conf) {

        BetaMemory memory;

        final List list = new ArrayList( 2 );
        if ( this.indexed0 ) {
            final VariableConstraint variableConstraint = (VariableConstraint) this.constraint0;
            final FieldIndex index = new FieldIndex( variableConstraint.getFieldExtractor(),
                                                     variableConstraint.getRequiredDeclarations()[0],
                                                     variableConstraint.getEvaluator() );
            list.add( index );

        }

        if ( this.indexed1 ) {
            final VariableConstraint variableConstraint = (VariableConstraint) this.constraint1;
            final FieldIndex index = new FieldIndex( variableConstraint.getFieldExtractor(),
                                                     variableConstraint.getRequiredDeclarations()[0],
                                                     variableConstraint.getEvaluator() );
            list.add( index );
        }

        if ( this.indexed2 ) {
            final VariableConstraint variableConstraint = (VariableConstraint) this.constraint2;
            final FieldIndex index = new FieldIndex( variableConstraint.getFieldExtractor(),
                                                     variableConstraint.getRequiredDeclarations()[0],
                                                     variableConstraint.getEvaluator() );
            list.add( index );
        }

        if ( !list.isEmpty() ) {
            final FieldIndex[] indexes = (FieldIndex[]) list.toArray( new FieldIndex[list.size()] );
            TupleMemory tupleMemory;
            if ( conf.isIndexLeftBetaMemory() ) {
                tupleMemory = new TupleIndexHashTable( indexes );
            } else {
                tupleMemory = new TupleHashTable();
            }

            FactHandleMemory factHandleMemory;
            if ( conf.isIndexRightBetaMemory() ) {
                factHandleMemory = new FactHandleIndexHashTable( indexes );
            } else {
                factHandleMemory = conf.isSequential() ? (FactHandleMemory) new FactList() : (FactHandleMemory) new FactHashTable();
            }
            memory = new BetaMemory( conf.isSequential() ? null : tupleMemory,
                                     factHandleMemory );
        } else {
            memory = new BetaMemory( conf.isSequential() ? null : new TupleHashTable(),
                                     conf.isSequential() ? (FactHandleMemory) new FactList() : (FactHandleMemory) new FactHashTable() );
        }

        return memory;
    }

    public int hashCode() {
        return this.constraint0.hashCode() ^ this.constraint1.hashCode() ^ this.constraint2.hashCode();
    }

    /* (non-Javadoc)
     * @see org.drools.common.BetaNodeConstraints#getConstraints()
     */
    public LinkedList getConstraints() {
        final LinkedList list = new LinkedList();
        list.add( new LinkedListEntry( this.constraint0 ) );
        list.add( new LinkedListEntry( this.constraint1 ) );
        list.add( new LinkedListEntry( this.constraint2 ) );
        return list;
    }

    /**
     * Determine if another object is equal to this.
     * 
     * @param object
     *            The object to test.
     * 
     * @return <code>true</code> if <code>object</code> is equal to this,
     *         otherwise <code>false</code>.
     */
    public boolean equals(final Object object) {
        if ( this == object ) {
            return true;
        }

        if ( object == null || getClass() != object.getClass() ) {
            return false;
        }

        final TripleBetaConstraints other = (TripleBetaConstraints) object;

        if ( this.constraint0 != other.constraint0 && !this.constraint0.equals( other.constraint0 ) ) {
            return false;
        }

        if ( this.constraint1 != other.constraint1 && !this.constraint1.equals( other.constraint1 ) ) {
            return false;
        }

        if ( this.constraint2 != other.constraint2 && !this.constraint2.equals( other.constraint2 ) ) {
            return false;
        }

        return true;
    }

}