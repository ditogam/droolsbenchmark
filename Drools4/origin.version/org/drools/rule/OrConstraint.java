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

package org.drools.rule;

import java.util.Arrays;

import org.drools.common.InternalWorkingMemory;
import org.drools.reteoo.ReteTuple;
import org.drools.util.ArrayUtils;

/**
 * A class to implement Multi-Field OR constraints, so user can do:
 * 
 * Person( hair == 'blue' || eyes == 'blue' )
 * 
 * @author etirelli
 *
 */
public class OrConstraint extends AbstractCompositeConstraint {

    private static final long serialVersionUID = 400L;

    public OrConstraint() {
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAllowed(Object object,
                             InternalWorkingMemory workingMemory) {
        if ( this.alphaConstraints.length > 0 ) {
            for ( int i = 0; i < this.alphaConstraints.length; i++ ) {
                if ( this.alphaConstraints[i].isAllowed( object,
                                                         workingMemory ) ) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAllowedCachedLeft(ContextEntry context,
                                       Object object) {
        if ( this.betaConstraints.length > 0 ) {
            for ( int i = 0; i < this.betaConstraints.length; i++ ) {
                if ( this.betaConstraints[i].isAllowedCachedLeft( ((MultiFieldConstraintContextEntry)context).contexts[i],
                                                                  object ) ) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAllowedCachedRight(ReteTuple tuple,
                                        ContextEntry context) {
        if ( this.betaConstraints.length > 0 ) {
            for ( int i = 0; i < this.betaConstraints.length; i++ ) {
                if ( this.betaConstraints[i].isAllowedCachedRight( tuple,
                                                                   ((MultiFieldConstraintContextEntry)context).contexts[i] ) ) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public int hashCode() {
        final int PRIME = 31;
        int result = 3; // to differentiate from AND constraint
        result = PRIME * result + ArrayUtils.hashCode( this.alphaConstraints );
        result = PRIME * result + ArrayUtils.hashCode( this.betaConstraints );
        result = PRIME * result + ArrayUtils.hashCode( this.requiredDeclarations );
        return result;
    }

    public boolean equals(final Object object) {
        if ( this == object ) {
            return true;
        }
        if ( object == null || object.getClass() != OrConstraint.class ) {
            return false;
        }
        final OrConstraint other = (OrConstraint) object;

        return Arrays.equals( this.alphaConstraints,
                              other.alphaConstraints ) && Arrays.equals( this.betaConstraints,
                                                                         other.betaConstraints ) && Arrays.equals( this.requiredDeclarations,
                                                                                                                   other.requiredDeclarations );
    }

}
