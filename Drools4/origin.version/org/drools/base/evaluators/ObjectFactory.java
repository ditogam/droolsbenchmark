package org.drools.base.evaluators;

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

import java.util.Collection;

import org.drools.base.BaseEvaluator;
import org.drools.base.ShadowProxy;
import org.drools.base.ValueType;
import org.drools.common.InternalWorkingMemory;
import org.drools.rule.VariableRestriction.ObjectVariableContextEntry;
import org.drools.rule.VariableRestriction.VariableContextEntry;
import org.drools.spi.Evaluator;
import org.drools.spi.Extractor;
import org.drools.spi.FieldValue;
import org.drools.util.ShadowProxyUtils;

/**
 * This is the misc "bucket" evaluator factory for objects.
 * It is fairly limited in operations, 
 * and what operations are available are dependent on the exact type.
 * 
 * This supports "<" and ">" etc by requiring objects to implement the comparable interface.
 * Of course, literals will not work with comparator, as it has no way
 * of converting from literal to the appropriate type.
 * 
 * @author Michael Neale
 */
public class ObjectFactory
    implements
    EvaluatorFactory {

    private static final long       serialVersionUID = 400L;
    private static EvaluatorFactory INSTANCE         = new ObjectFactory();

    private ObjectFactory() {

    }

    public static EvaluatorFactory getInstance() {
        if ( ObjectFactory.INSTANCE == null ) {
            ObjectFactory.INSTANCE = new ObjectFactory();
        }
        return ObjectFactory.INSTANCE;
    }

    public Evaluator getEvaluator(final Operator operator) {
        if ( operator == Operator.EQUAL ) {
            return ObjectEqualEvaluator.INSTANCE;
        } else if ( operator == Operator.NOT_EQUAL ) {
            return ObjectNotEqualEvaluator.INSTANCE;
        } else if ( operator == Operator.LESS ) {
            return ObjectLessEvaluator.INSTANCE;
        } else if ( operator == Operator.LESS_OR_EQUAL ) {
            return ObjectLessOrEqualEvaluator.INSTANCE;
        } else if ( operator == Operator.GREATER ) {
            return ObjectGreaterEvaluator.INSTANCE;
        } else if ( operator == Operator.GREATER_OR_EQUAL ) {
            return ObjectGreaterOrEqualEvaluator.INSTANCE;
        } else if ( operator == Operator.CONTAINS ) {
            return ObjectContainsEvaluator.INSTANCE;
        } else if ( operator == Operator.EXCLUDES ) {
            return ObjectExcludesEvaluator.INSTANCE;
        } else if ( operator == Operator.NOT_CONTAINS) {
            return ObjectExcludesEvaluator.INSTANCE; // 'not contains' and 'excludes' are synonyms
        } else if ( operator == Operator.MEMBEROF ) {
            return ObjectMemberOfEvaluator.INSTANCE;
        } else if ( operator == Operator.NOTMEMBEROF ) {
            return ObjectNotMemberOfEvaluator.INSTANCE;
        } else {
            throw new RuntimeException( "Operator '" + operator + "' does not exist for ObjectEvaluator" );
        }
    }

    static class ObjectEqualEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long     serialVersionUID = 400L;
        public final static Evaluator INSTANCE         = new ObjectEqualEvaluator();

        private ObjectEqualEvaluator() {
            super( ValueType.OBJECT_TYPE,
                   Operator.EQUAL );
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor,
                                final Object object1, final FieldValue object2) {
            final Object value1 = extractor.getValue( workingMemory, object1 );
            final Object value2 = object2.getValue();
            if ( value1 == null ) {
                return value2 == null;
            }
            if( value2 != null && value2 instanceof ShadowProxy ) {
                return value2.equals( value1 );
            }
            return value1.equals( value2 );
        }

        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                           final VariableContextEntry context, final Object left) {
            final Object value = context.declaration.getExtractor().getValue( workingMemory, left );
            if ( value == null ) {
                return ((ObjectVariableContextEntry) context).right == null;
            }
            if( ((ObjectVariableContextEntry) context).right != null && ((ObjectVariableContextEntry) context).right instanceof ShadowProxy ) {
                return ((ObjectVariableContextEntry) context).right.equals( value );
            }
            return value.equals( ((ObjectVariableContextEntry) context).right );
        }

        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                          final VariableContextEntry context, final Object right) {
            final Object value = context.extractor.getValue( workingMemory, right );
            if ( ((ObjectVariableContextEntry) context).left == null ) {
                return value == null;
            }
            if( value != null && value instanceof ShadowProxy ) {
                return value.equals( ((ObjectVariableContextEntry) context).left );
            }
            return ((ObjectVariableContextEntry) context).left.equals( value );
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor1,
                                final Object object1,
                                final Extractor extractor2, final Object object2) {
            final Object value1 = extractor1.getValue( workingMemory, object1 );
            final Object value2 = extractor2.getValue( workingMemory, object2 );
            if ( value1 == null ) {
                return value2 == null;
            }
            if( value2 != null && value2 instanceof ShadowProxy ) {
                return value2.equals( value1 );
            }
            return value1.equals( value2 );
        }

        public String toString() {
            return "Object ==";
        }

    }

    static class ObjectNotEqualEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long     serialVersionUID = 400L;
        public final static Evaluator INSTANCE         = new ObjectNotEqualEvaluator();

        private ObjectNotEqualEvaluator() {
            super( ValueType.OBJECT_TYPE,
                   Operator.NOT_EQUAL );
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor,
                                final Object object1, final FieldValue object2) {
            final Object value1 = extractor.getValue( workingMemory, object1 );
            final Object value2 = object2.getValue();
            if ( value1 == null ) {
                return value2 != null;
            }
            if( value2 != null && value2 instanceof ShadowProxy ) {
                return !value2.equals( value1 );
            }
            return !value1.equals( value2 );
        }

        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                           final VariableContextEntry context, final Object left) {
            final Object value = context.declaration.getExtractor().getValue( workingMemory, left );
            if ( value == null ) {
                return ((ObjectVariableContextEntry) context).right != null;
            }
            if( ((ObjectVariableContextEntry) context).right != null && ((ObjectVariableContextEntry) context).right instanceof ShadowProxy ) {
                return !((ObjectVariableContextEntry) context).right.equals( value );
            }
            return !value.equals( ((ObjectVariableContextEntry) context).right );
        }

        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                          final VariableContextEntry context, final Object right) {
            final Object value = context.extractor.getValue( workingMemory, right );
            if ( ((ObjectVariableContextEntry) context).left == null ) {
                return value != null;
            }
            if( value != null && value instanceof ShadowProxy ) {
                return !value.equals( ((ObjectVariableContextEntry) context).left );
            }
            return !((ObjectVariableContextEntry) context).left.equals( value );
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor1,
                                final Object object1,
                                final Extractor extractor2, final Object object2) {
            final Object value1 = extractor1.getValue( workingMemory, object1 );
            final Object value2 = extractor2.getValue( workingMemory, object2 );
            if ( value1 == null ) {
                return value2 != null;
            }
            if( value2 != null && value2 instanceof ShadowProxy ) {
                return !value2.equals( value1 );
            }
            return !value1.equals( value2 );
        }

        public String toString() {
            return "Object !=";
        }
    }

    static class ObjectLessEvaluator extends BaseEvaluator {
        private static final long     serialVersionUID = 400L;
        public final static Evaluator INSTANCE         = new ObjectLessEvaluator();

        private ObjectLessEvaluator() {
            super( ValueType.OBJECT_TYPE,
                   Operator.LESS );
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor,
                                final Object object1, final FieldValue object2) {
            if( extractor.isNullValue( workingMemory, object1 ) ) {
                return false;
            }
            final Comparable comp = (Comparable) extractor.getValue( workingMemory, object1 );
            return comp.compareTo( object2.getValue() ) < 0;
        }

        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                           final VariableContextEntry context, final Object left) {
            if( context.rightNull ) {
                return false;
            }
            final Comparable comp = (Comparable) ((ObjectVariableContextEntry) context).right;
            return comp.compareTo( context.declaration.getExtractor().getValue( workingMemory, left ) ) < 0;
        }

        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                          final VariableContextEntry context, final Object right) {
            if( context.extractor.isNullValue( workingMemory, right ) ) {
                return false;
            }
            final Comparable comp = (Comparable) context.extractor.getValue( workingMemory, right );
            return comp.compareTo( ((ObjectVariableContextEntry) context).left ) < 0;
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor1,
                                final Object object1,
                                final Extractor extractor2, final Object object2) {
            if( extractor1.isNullValue( workingMemory, object1 ) ) {
                return false;
            }
            final Comparable comp = (Comparable) extractor1.getValue( workingMemory, object1 );
            return comp.compareTo( extractor2.getValue( workingMemory, object2 ) ) < 0;
        }

        public String toString() {
            return "Object <";
        }
    }

    static class ObjectLessOrEqualEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long     serialVersionUID = 400L;
        public final static Evaluator INSTANCE         = new ObjectLessOrEqualEvaluator();

        private ObjectLessOrEqualEvaluator() {
            super( ValueType.OBJECT_TYPE,
                   Operator.LESS_OR_EQUAL );
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor,
                                final Object object1, final FieldValue object2) {
            if( extractor.isNullValue( workingMemory, object1 ) ) {
                return false;
            }
            final Comparable comp = (Comparable) extractor.getValue( workingMemory, object1 );
            return comp.compareTo( object2.getValue() ) <= 0;
        }

        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                           final VariableContextEntry context, final Object left) {
            if( context.rightNull ) {
                return false;
            }
            final Comparable comp = (Comparable) ((ObjectVariableContextEntry) context).right;
            return comp.compareTo( context.declaration.getExtractor().getValue( workingMemory, left ) ) <= 0;
        }

        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                          final VariableContextEntry context, final Object right) {
            if( context.extractor.isNullValue( workingMemory, right ) ) {
                return false;
            }
            final Comparable comp = (Comparable) context.extractor.getValue( workingMemory, right );
            return comp.compareTo( ((ObjectVariableContextEntry) context).left ) <= 0;
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor1,
                                final Object object1,
                                final Extractor extractor2, final Object object2) {
            if( extractor1.isNullValue( workingMemory, object1 ) ) {
                return false;
            }
            final Comparable comp = (Comparable) extractor1.getValue( workingMemory, object1 );
            return comp.compareTo( extractor2.getValue( workingMemory, object2 ) ) <= 0;
        }

        public String toString() {
            return "Object <=";
        }
    }

    static class ObjectGreaterEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long     serialVersionUID = 400L;
        public final static Evaluator INSTANCE         = new ObjectGreaterEvaluator();

        private ObjectGreaterEvaluator() {
            super( ValueType.OBJECT_TYPE,
                   Operator.GREATER );
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor,
                                final Object object1, final FieldValue object2) {
            if( extractor.isNullValue( workingMemory, object1 ) ) {
                return false;
            }
            final Comparable comp = (Comparable) extractor.getValue( workingMemory, object1 );
            return comp.compareTo( object2.getValue() ) > 0;
        }

        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                           final VariableContextEntry context, final Object left) {
            if( context.rightNull ) {
                return false;
            }
            final Comparable comp = (Comparable) ((ObjectVariableContextEntry) context).right;
            return comp.compareTo( context.declaration.getExtractor().getValue( workingMemory, left ) ) > 0;
        }

        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                          final VariableContextEntry context, final Object right) {
            if( context.extractor.isNullValue( workingMemory, right ) ) {
                return false;
            }
            final Comparable comp = (Comparable) context.extractor.getValue( workingMemory, right );
            return comp.compareTo( ((ObjectVariableContextEntry) context).left ) > 0;
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor1,
                                final Object object1,
                                final Extractor extractor2, final Object object2) {
            if( extractor1.isNullValue( workingMemory, object1 ) ) {
                return false;
            }
            final Comparable comp = (Comparable) extractor1.getValue( workingMemory, object1 );
            return comp.compareTo( extractor2.getValue( workingMemory, object2 ) ) > 0;
        }

        public String toString() {
            return "Object >";
        }
    }

    static class ObjectGreaterOrEqualEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long     serialVersionUID = 400L;
        public final static Evaluator INSTANCE         = new ObjectGreaterOrEqualEvaluator();

        private ObjectGreaterOrEqualEvaluator() {
            super( ValueType.OBJECT_TYPE,
                   Operator.GREATER_OR_EQUAL );
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor,
                                final Object object1, final FieldValue object2) {
            if( extractor.isNullValue( workingMemory, object1 ) ) {
                return false;
            }
            final Comparable comp = (Comparable) extractor.getValue( workingMemory, object1 );
            return comp.compareTo( object2.getValue() ) >= 0;
        }

        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                           final VariableContextEntry context, final Object left) {
            if( context.rightNull ) {
                return false;
            }
            final Comparable comp = (Comparable) ((ObjectVariableContextEntry) context).right;
            return comp.compareTo( context.declaration.getExtractor().getValue( workingMemory, left ) ) >= 0;
        }

        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                          final VariableContextEntry context, final Object right) {
            if( context.extractor.isNullValue( workingMemory, right ) ) {
                return false;
            }
            final Comparable comp = (Comparable) context.extractor.getValue( workingMemory, right );
            return comp.compareTo( ((ObjectVariableContextEntry) context).left ) >= 0;
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor1,
                                final Object object1,
                                final Extractor extractor2, final Object object2) {
            if( extractor1.isNullValue( workingMemory, object1 ) ) {
                return false;
            }
            final Comparable comp = (Comparable) extractor1.getValue( workingMemory, object1 );
            return comp.compareTo( extractor2.getValue( workingMemory, object2 ) ) >= 0;
        }

        public String toString() {
            return "Object >=";
        }
    }

    static class ObjectContainsEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long     serialVersionUID = 400L;
        public final static Evaluator INSTANCE         = new ObjectContainsEvaluator();

        private ObjectContainsEvaluator() {
            super( ValueType.OBJECT_TYPE,
                   Operator.CONTAINS );
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor,
                                final Object object1, final FieldValue object2) {
            final Object value = object2.getValue();
            final Collection col = (Collection) extractor.getValue( workingMemory, object1 );
            return ( col == null ) ? false : ShadowProxyUtils.contains( col, value );
        }

        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                           final VariableContextEntry context, final Object left) {
            final Object value = context.declaration.getExtractor().getValue( workingMemory, left );
            final Collection col = (Collection) ((ObjectVariableContextEntry) context).right;
            return ( col == null ) ? false : ShadowProxyUtils.contains( col, value );
        }

        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                          final VariableContextEntry context, final Object right) {
            final Object value = ((ObjectVariableContextEntry) context).left;
            final Collection col = (Collection) context.extractor.getValue( workingMemory, right );
            return ( col == null ) ? false : ShadowProxyUtils.contains( col, value );
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor1,
                                final Object object1,
                                final Extractor extractor2, final Object object2) {
            final Object value = extractor2.getValue( workingMemory, object2 );
            final Collection col = (Collection) extractor1.getValue( workingMemory, object1 );
            return ( col == null ) ? false : ShadowProxyUtils.contains( col, value );
        }

        public String toString() {
            return "Object contains";
        }
    }

    static class ObjectExcludesEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long     serialVersionUID = 400L;
        public final static Evaluator INSTANCE         = new ObjectExcludesEvaluator();

        private ObjectExcludesEvaluator() {
            super( ValueType.OBJECT_TYPE,
                   Operator.EXCLUDES );
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor,
                                final Object object1, final FieldValue object2) {
            final Object value = object2.getValue();
            final Collection col = (Collection) extractor.getValue( workingMemory, object1 );
            return ( col == null ) ? true : !ShadowProxyUtils.contains( col, value );
        }

        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                           final VariableContextEntry context, final Object left) {
            final Object value = context.declaration.getExtractor().getValue( workingMemory, left );
            final Collection col = (Collection) ((ObjectVariableContextEntry) context).right;
            return ( col == null ) ? true : !ShadowProxyUtils.contains( col, value );
        }

        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                          final VariableContextEntry context, final Object right) {
            final Object value = ((ObjectVariableContextEntry) context).left;
            final Collection col = (Collection) context.extractor.getValue( workingMemory, right );
            return ( col == null ) ? true : !ShadowProxyUtils.contains( col, value );
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor1,
                                final Object object1,
                                final Extractor extractor2, final Object object2) {
            final Object value = extractor2.getValue( workingMemory, object2 );
            final Collection col = (Collection) extractor1.getValue( workingMemory, object1 );
            return ( col == null ) ? true : !ShadowProxyUtils.contains( col, value );
        }

        public String toString() {
            return "Object excludes";
        }
    }

    static class ObjectMemberOfEvaluator extends BaseMemberOfEvaluator {

        private static final long     serialVersionUID = 400L;
        public final static Evaluator INSTANCE         = new ObjectMemberOfEvaluator();

        private ObjectMemberOfEvaluator() {
            super( ValueType.OBJECT_TYPE,
                   Operator.MEMBEROF );
        }

        public String toString() {
            return "Object memberOf";
        }
    }

    static class ObjectNotMemberOfEvaluator extends BaseNotMemberOfEvaluator {

        private static final long     serialVersionUID = 400L;
        public final static Evaluator INSTANCE         = new ObjectNotMemberOfEvaluator();

        private ObjectNotMemberOfEvaluator() {
            super( ValueType.OBJECT_TYPE,
                   Operator.NOTMEMBEROF );
        }

        public String toString() {
            return "Object not memberOf";
        }
    }
    
    
}