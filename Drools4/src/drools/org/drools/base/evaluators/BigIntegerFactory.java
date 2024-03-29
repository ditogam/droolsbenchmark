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

import java.math.BigInteger;

import org.drools.base.BaseEvaluator;
import org.drools.base.ValueType;
import org.drools.common.InternalWorkingMemory;
import org.drools.rule.VariableRestriction.ObjectVariableContextEntry;
import org.drools.rule.VariableRestriction.VariableContextEntry;
import org.drools.spi.Evaluator;
import org.drools.spi.Extractor;
import org.drools.spi.FieldValue;

public class BigIntegerFactory
    implements
    EvaluatorFactory {

    private static final long       serialVersionUID = 400L;
    private static EvaluatorFactory INSTANCE         = new BigIntegerFactory();

    private BigIntegerFactory() {

    }

    public static EvaluatorFactory getInstance() {
        if ( BigIntegerFactory.INSTANCE == null ) {
            BigIntegerFactory.INSTANCE = new BigIntegerFactory();
        }
        return BigIntegerFactory.INSTANCE;
    }

    public Evaluator getEvaluator(final Operator operator) {
        if ( operator == Operator.EQUAL ) {
            return BigIntegerEqualEvaluator.INSTANCE;
        } else if ( operator == Operator.NOT_EQUAL ) {
            return BigIntegerNotEqualEvaluator.INSTANCE;
        } else if ( operator == Operator.LESS ) {
            return BigIntegerLessEvaluator.INSTANCE;
        } else if ( operator == Operator.LESS_OR_EQUAL ) {
            return BigIntegerLessOrEqualEvaluator.INSTANCE;
        } else if ( operator == Operator.GREATER ) {
            return BigIntegerGreaterEvaluator.INSTANCE;
        } else if ( operator == Operator.GREATER_OR_EQUAL ) {
            return BigIntegerGreaterOrEqualEvaluator.INSTANCE;
        } else if ( operator == Operator.MEMBEROF ) {
            return BigIntegerMemberOfEvaluator.INSTANCE;
        } else if ( operator == Operator.NOTMEMBEROF ) {
            return BigIntegerNotMemberOfEvaluator.INSTANCE;
        } else {
            throw new RuntimeException( "Operator '" + operator + "' does not exist for BigIntegerEvaluator" );
        }
    }

    static class BigIntegerEqualEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long     serialVersionUID = 400L;
        public final static Evaluator INSTANCE         = new BigIntegerEqualEvaluator();

        private BigIntegerEqualEvaluator() {
            super( ValueType.BIG_INTEGER_TYPE,
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
            return value1.equals( value2 );
        }

        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                           final VariableContextEntry context, final Object left) {
            final Object value = context.declaration.getExtractor().getValue( workingMemory, left );
            if ( value == null ) {
                return ((ObjectVariableContextEntry) context).right == null;
            }
            return value.equals( ((ObjectVariableContextEntry) context).right );
        }

        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                          final VariableContextEntry context, final Object right) {
            final Object value = context.extractor.getValue( workingMemory, right );
            if ( ((ObjectVariableContextEntry) context).left == null ) {
                return value == null;
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
            return value1.equals( value2 );
        }

        public String toString() {
            return "BigInteger ==";
        }
    }

    static class BigIntegerNotEqualEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long     serialVersionUID = 400L;
        public final static Evaluator INSTANCE         = new BigIntegerNotEqualEvaluator();

        private BigIntegerNotEqualEvaluator() {
            super( ValueType.BIG_INTEGER_TYPE,
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
            return !value1.equals( value2 );
        }

        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                           final VariableContextEntry context, final Object left) {
            final Object value = context.declaration.getExtractor().getValue( workingMemory, left );
            if ( value == null ) {
                return ((ObjectVariableContextEntry) context).right != null;
            }
            return !value.equals( ((ObjectVariableContextEntry) context).right );
        }

        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                          final VariableContextEntry context, final Object right) {
            final Object value = context.extractor.getValue( workingMemory, right );
            if ( ((ObjectVariableContextEntry) context).left == null ) {
                return value != null;
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
            return !value1.equals( value2 );
        }

        public String toString() {
            return "BigInteger !=";
        }
    }

    static class BigIntegerLessEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long     serialVersionUID = 400L;
        public final static Evaluator INSTANCE         = new BigIntegerLessEvaluator();

        private BigIntegerLessEvaluator() {
            super( ValueType.BIG_INTEGER_TYPE,
                   Operator.LESS );
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor,
                                final Object object1, final FieldValue object2) {
            if( extractor.isNullValue( workingMemory, object1 ) ) {
                return false;
            }
            final BigInteger comp = (BigInteger) extractor.getValue( workingMemory, object1 );
            return comp.compareTo( (BigInteger) object2.getValue() ) < 0;
        }

        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                           final VariableContextEntry context, final Object left) {
            if( context.rightNull ) {
                return false;
            }
            final BigInteger comp = (BigInteger) ((ObjectVariableContextEntry) context).right;
            return comp.compareTo( (BigInteger) context.declaration.getExtractor().getValue( workingMemory, left ) ) < 0;
        }

        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                          final VariableContextEntry context, final Object right) {
            if( context.extractor.isNullValue( workingMemory, right ) ) {
                return false;
            }
            final BigInteger comp = (BigInteger) context.extractor.getValue( workingMemory, right );
            return comp.compareTo( (BigInteger) ((ObjectVariableContextEntry) context).left ) < 0;
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor1,
                                final Object object1,
                                final Extractor extractor2, final Object object2) {
            if( extractor1.isNullValue( workingMemory, object1 ) ) {
                return false;
            }
            final BigInteger comp = (BigInteger) extractor1.getValue( workingMemory, object1 );
            return comp.compareTo( (BigInteger) extractor2.getValue( workingMemory, object2 ) ) < 0;
        }

        public String toString() {
            return "BigInteger <";
        }
    }

    static class BigIntegerLessOrEqualEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long     serialVersionUID = 400L;
        public final static Evaluator INSTANCE         = new BigIntegerLessOrEqualEvaluator();

        private BigIntegerLessOrEqualEvaluator() {
            super( ValueType.BIG_INTEGER_TYPE,
                   Operator.LESS_OR_EQUAL );
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor,
                                final Object object1, final FieldValue object2) {
            if( extractor.isNullValue( workingMemory, object1 ) ) {
                return false;
            }
            final BigInteger comp = (BigInteger) extractor.getValue( workingMemory, object1 );
            return comp.compareTo( (BigInteger) object2.getValue() ) <= 0;
        }

        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                           final VariableContextEntry context, final Object left) {
            if( context.rightNull ) {
                return false;
            }
            final BigInteger comp = (BigInteger) ((ObjectVariableContextEntry) context).right;
            return comp.compareTo( (BigInteger) context.declaration.getExtractor().getValue( workingMemory, left ) ) <= 0;
        }

        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                          final VariableContextEntry context, final Object right) {
            if( context.extractor.isNullValue( workingMemory, right ) ) {
                return false;
            }
            final BigInteger comp = (BigInteger) context.extractor.getValue( workingMemory, right );
            return comp.compareTo( (BigInteger) ((ObjectVariableContextEntry) context).left ) <= 0;
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor1,
                                final Object object1,
                                final Extractor extractor2, final Object object2) {
            if( extractor1.isNullValue( workingMemory, object1 ) ) {
                return false;
            }
            final BigInteger comp = (BigInteger) extractor1.getValue( workingMemory, object1 );
            return comp.compareTo( (BigInteger) extractor2.getValue( workingMemory, object2 ) ) <= 0;
        }

        public String toString() {
            return "BigInteger <=";
        }
    }

    static class BigIntegerGreaterEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long     serialVersionUID = 400L;
        public final static Evaluator INSTANCE         = new BigIntegerGreaterEvaluator();

        private BigIntegerGreaterEvaluator() {
            super( ValueType.BIG_INTEGER_TYPE,
                   Operator.GREATER );
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor,
                                final Object object1, final FieldValue object2) {
            if( extractor.isNullValue( workingMemory, object1 ) ) {
                return false;
            }
            final BigInteger comp = (BigInteger) extractor.getValue( workingMemory, object1 );
            return comp.compareTo( (BigInteger) object2.getValue() ) > 0;
        }

        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                           final VariableContextEntry context, final Object left) {
            if( context.rightNull ) {
                return false;
            }
            final BigInteger comp = (BigInteger) ((ObjectVariableContextEntry) context).right;
            return comp.compareTo( (BigInteger) context.declaration.getExtractor().getValue( workingMemory, left ) ) > 0;
        }

        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                          final VariableContextEntry context, final Object right) {
            if( context.extractor.isNullValue( workingMemory, right ) ) {
                return false;
            }
            final BigInteger comp = (BigInteger) context.extractor.getValue( workingMemory, right );
            return comp.compareTo( (BigInteger) ((ObjectVariableContextEntry) context).left ) > 0;
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor1,
                                final Object object1,
                                final Extractor extractor2, final Object object2) {
            if( extractor1.isNullValue( workingMemory, object1 ) ) {
                return false;
            }
            final BigInteger comp = (BigInteger) extractor1.getValue( workingMemory, object1 );
            return comp.compareTo( (BigInteger) extractor2.getValue( workingMemory, object2 ) ) > 0;
        }

        public String toString() {
            return "BigInteger >";
        }
    }

    static class BigIntegerGreaterOrEqualEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long      serialVersionUID = 400L;
        private final static Evaluator INSTANCE         = new BigIntegerGreaterOrEqualEvaluator();

        private BigIntegerGreaterOrEqualEvaluator() {
            super( ValueType.BIG_INTEGER_TYPE,
                   Operator.GREATER_OR_EQUAL );
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor,
                                final Object object1, final FieldValue object2) {
            if( extractor.isNullValue( workingMemory, object1 ) ) {
                return false;
            }
            final BigInteger comp = (BigInteger) extractor.getValue( workingMemory, object1 );
            return comp.compareTo( (BigInteger) object2.getValue() ) >= 0;
        }

        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                           final VariableContextEntry context, final Object left) {
            if( context.rightNull ) {
                return false;
            }
            final BigInteger comp = (BigInteger) ((ObjectVariableContextEntry) context).right;
            return comp.compareTo( (BigInteger) context.declaration.getExtractor().getValue( workingMemory, left ) ) >= 0;
        }

        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                          final VariableContextEntry context, final Object right) {
            if( context.extractor.isNullValue( workingMemory, right ) ) {
                return false;
            }
            final BigInteger comp = (BigInteger) context.extractor.getValue( workingMemory, right );
            return comp.compareTo( (BigInteger) ((ObjectVariableContextEntry) context).left ) >= 0;
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final Extractor extractor1,
                                final Object object1,
                                final Extractor extractor2, final Object object2) {
            if( extractor1.isNullValue( workingMemory, object1 ) ) {
                return false;
            }
            final BigInteger comp = (BigInteger) extractor1.getValue( workingMemory, object1 );
            return comp.compareTo( (BigInteger) extractor2.getValue( workingMemory, object2 ) ) >= 0;
        }

        public String toString() {
            return "BigInteger >=";
        }
    }
    
    static class BigIntegerMemberOfEvaluator extends BaseMemberOfEvaluator {

        private static final long     serialVersionUID = 400L;
        public final static Evaluator INSTANCE         = new BigIntegerMemberOfEvaluator();

        private BigIntegerMemberOfEvaluator() {
            super( ValueType.BIG_INTEGER_TYPE,
                   Operator.MEMBEROF );
        }

        public String toString() {
            return "BigInteger memberOf";
        }
    }

    static class BigIntegerNotMemberOfEvaluator extends BaseNotMemberOfEvaluator {
        /**
         * 
         */
        private static final long     serialVersionUID = 400L;
        public final static Evaluator INSTANCE         = new BigIntegerNotMemberOfEvaluator();

        private BigIntegerNotMemberOfEvaluator() {
            super( ValueType.BIG_INTEGER_TYPE,
                   Operator.NOTMEMBEROF );
        }

        public String toString() {
            return "BigInteger not memberOf";
        }
    }
    
}