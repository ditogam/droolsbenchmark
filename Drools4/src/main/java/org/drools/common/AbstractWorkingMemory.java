//package org.drools.common;
//
///*
// * Copyright 2005 JBoss Inc
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//import java.beans.PropertyChangeEvent;
//import java.beans.PropertyChangeListener;
//import java.io.Serializable;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import org.drools.Agenda;
//import org.drools.FactException;
//import org.drools.FactHandle;
//import org.drools.ObjectFilter;
//import org.drools.Otherwise;
//import org.drools.QueryResults;
//import org.drools.RuleBase;
//import org.drools.RuleBaseConfiguration;
//import org.drools.RuleBaseConfiguration.AssertBehaviour;
//import org.drools.RuleBaseConfiguration.LogicalOverride;
//import org.drools.RuntimeDroolsException;
//import org.drools.WorkingMemory;
//import org.drools.base.MapGlobalResolver;
//import org.drools.base.ShadowProxy;
//import org.drools.event.AgendaEventListener;
//import org.drools.event.AgendaEventSupport;
//import org.drools.event.RuleBaseEventListener;
//import org.drools.event.RuleFlowEventListener;
//import org.drools.event.RuleFlowEventSupport;
//import org.drools.event.WorkingMemoryEventListener;
//import org.drools.event.WorkingMemoryEventSupport;
//import org.drools.reteoo.LIANodePropagation;
//import org.drools.rule.Declaration;
//import org.drools.rule.Rule;
//import org.drools.ruleflow.common.core.Process;
//import org.drools.ruleflow.common.instance.ProcessInstance;
//import org.drools.ruleflow.core.RuleFlowProcess;
//import org.drools.ruleflow.instance.RuleFlowProcessInstance;
//import org.drools.ruleflow.instance.impl.RuleFlowProcessInstanceImpl;
//import org.drools.spi.Activation;
//import org.drools.spi.AgendaFilter;
//import org.drools.spi.AgendaGroup;
//import org.drools.spi.AsyncExceptionHandler;
//import org.drools.spi.FactHandleFactory;
//import org.drools.spi.GlobalResolver;
//import org.drools.spi.PropagationContext;
//import org.drools.util.AbstractHashTable.HashTableIterator;
//import org.drools.util.JavaIteratorAdapter;
//import org.drools.util.ObjectHashMap;
//import org.drools.util.PrimitiveLongMap;
//import org.drools.util.concurrent.locks.Lock;
//import org.drools.util.concurrent.locks.ReentrantLock;
//
///**
// * Implementation of <code>WorkingMemory</code>.
// *
// * @author <a href="mailto:bob@werken.com">bob mcwhirter </a>
// * @author <a href="mailto:mark.proctor@jboss.com">Mark Proctor</a>
// * @author <a href="mailto:simon@redhillconsulting.com.au">Simon Harris </a>
// */
//public abstract class AbstractWorkingMemory
//    implements
//    InternalWorkingMemoryActions,
//    EventSupport,
//    PropertyChangeListener {
//    // ------------------------------------------------------------
//    // Constants
//    // ------------------------------------------------------------
//    protected static final Class[] ADD_REMOVE_PROPERTY_CHANGE_LISTENER_ARG_TYPES = new Class[]{PropertyChangeListener.class};
//
//    // ------------------------------------------------------------
//    // Instance members
//    // ------------------------------------------------------------
//    protected final long id;
//
//    /**
//     * The arguments used when adding/removing a property change listener.
//     */
//    protected final Object[] addRemovePropertyChangeListenerArgs = new Object[]{this};
//
//    /**
//     * The actual memory for the <code>JoinNode</code>s.
//     */
//    protected final PrimitiveLongMap nodeMemories = new PrimitiveLongMap(32,
//        8);
//    /**
//     * Object-to-handle mapping.
//     */
//    private final ObjectHashMap assertMap;
//    private final ObjectHashMap identityMap;
//
//    protected Map queryResults = Collections.EMPTY_MAP;
//
//    /**
//     * Global values which are associated with this memory.
//     */
//    protected GlobalResolver globalResolver;
//
//    protected static final Object NULL = new Serializable() {
//        private static final long serialVersionUID = 400L;
//    };
//
//    /**
//     * The eventSupport
//     */
//    protected WorkingMemoryEventSupport workingMemoryEventSupport = new WorkingMemoryEventSupport();
//
//    protected AgendaEventSupport agendaEventSupport = new AgendaEventSupport();
//
//    protected RuleFlowEventSupport ruleFlowEventSupport = new RuleFlowEventSupport();
//
//    /**
//     * The <code>RuleBase</code> with which this memory is associated.
//     */
//    protected transient InternalRuleBase ruleBase;
//
//    protected final FactHandleFactory handleFactory;
//
//    protected final TruthMaintenanceSystem tms;
//
//    /**
//     * Rule-firing agenda.
//     */
//    protected DefaultAgenda agenda;
//
//    protected final List actionQueue = new ArrayList();
//
//    protected final ReentrantLock lock = new ReentrantLock();
//
//    protected final boolean discardOnLogicalOverride;
//
//    protected long propagationIdCounter;
//
//    private final boolean maintainTms;
//
//    private final boolean sequential;
//
//    private List liaPropagations = Collections.EMPTY_LIST;
//
//    /**
//     * Flag to determine if a rule is currently being fired.
//     */
//    protected boolean firing;
//
//    protected boolean halt;
//
//    private int processCounter;
//
//    // ------------------------------------------------------------
//    // Constructors
//    // ------------------------------------------------------------
//
//    /**
//     * Construct.
//     *
//     * @param ruleBase The backing rule-base.
//     */
//    public AbstractWorkingMemory(final int id,
//                                 final InternalRuleBase ruleBase,
//                                 final FactHandleFactory handleFactory) {
//        this.id = id;
//        this.ruleBase = ruleBase;
//        this.handleFactory = handleFactory;
//        this.globalResolver = new MapGlobalResolver();
//        this.maintainTms = this.ruleBase.getConfiguration().isMaintainTms();
//        this.sequential = this.ruleBase.getConfiguration().isSequential();
//
//        if (this.maintainTms) {
//            this.tms = new TruthMaintenanceSystem(this);
//        } else {
//            this.tms = null;
//        }
//
//        this.assertMap = new ObjectHashMap();
//        final RuleBaseConfiguration conf = this.ruleBase.getConfiguration();
//
//        if (conf.getAssertBehaviour() == AssertBehaviour.IDENTITY) {
//            this.assertMap.setComparator(new IdentityAssertMapComparator());
//            this.identityMap = assertMap;
//        } else {
//            this.assertMap.setComparator(new EqualityAssertMapComparator());
//            this.identityMap = new ObjectHashMap();
//            this.identityMap.setComparator(new IdentityAssertMapComparator());
//        }
//
//        // Only takes effect if are using idententity behaviour for assert
//        if (conf.getLogicalOverride() == LogicalOverride.DISCARD) {
//            this.discardOnLogicalOverride = true;
//        } else {
//            this.discardOnLogicalOverride = false;
//        }
//    }
//
//    // ------------------------------------------------------------
//    // Instance methods
//    // ------------------------------------------------------------
//
//    void setRuleBase(final InternalRuleBase ruleBase) {
//        this.ruleBase = ruleBase;
//    }
//
//    public void setWorkingMemoryEventSupport(WorkingMemoryEventSupport workingMemoryEventSupport) {
//        this.workingMemoryEventSupport = workingMemoryEventSupport;
//    }
//
//    public void setAgendaEventSupport(AgendaEventSupport agendaEventSupport) {
//        this.agendaEventSupport = agendaEventSupport;
//    }
//
//    public void setRuleFlowEventSupport(RuleFlowEventSupport ruleFlowEventSupport) {
//        this.ruleFlowEventSupport = ruleFlowEventSupport;
//    }
//
//    public boolean isSequential() {
//        return this.sequential;
//    }
//
//    public void addLIANodePropagation(LIANodePropagation liaNodePropagation) {
//        if (this.liaPropagations == Collections.EMPTY_LIST) {
//            this.liaPropagations = new ArrayList();
//        }
//        this.liaPropagations.add(liaNodePropagation);
//    }
//
//    public void addEventListener(final WorkingMemoryEventListener listener) {
//        try {
//            //this.lock.lock();
//            this.workingMemoryEventSupport.addEventListener(listener);
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public void removeEventListener(final WorkingMemoryEventListener listener) {
//        try {
//            //this.lock.lock();
//            this.workingMemoryEventSupport.removeEventListener(listener);
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public List getWorkingMemoryEventListeners() {
//        try {
//            //this.lock.lock();
//            return this.workingMemoryEventSupport.getEventListeners();
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public void addEventListener(final AgendaEventListener listener) {
//        try {
//            //this.lock.lock();
//            this.agendaEventSupport.addEventListener(listener);
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public void removeEventListener(final AgendaEventListener listener) {
//        try {
//            //this.lock.lock();
//            this.agendaEventSupport.removeEventListener(listener);
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public List getAgendaEventListeners() {
//        try {
//            //this.lock.lock();
//            return this.agendaEventSupport.getEventListeners();
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public void addEventListener(final RuleFlowEventListener listener) {
//        try {
//            //this.lock.lock();
//            this.ruleFlowEventSupport.addEventListener(listener);
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public void removeEventListener(final RuleFlowEventListener listener) {
//        try {
//            //this.lock.lock();
//            this.ruleFlowEventSupport.removeEventListener(listener);
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public List getRuleFlowEventListeners() {
//        try {
//            //this.lock.lock();
//            return this.ruleFlowEventSupport.getEventListeners();
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public void addEventListener(RuleBaseEventListener listener) {
//        try {
//            //this.lock.lock();
//            this.ruleBase.addEventListener(listener);
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public List getRuleBaseEventListeners() {
//        try {
//            //this.lock.lock();
//            return this.ruleBase.getRuleBaseEventListeners();
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public void removeEventListener(RuleBaseEventListener listener) {
//        try {
//            //this.lock.lock();
//            this.ruleBase.removeEventListener(listener);
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public FactHandleFactory getFactHandleFactory() {
//        return this.handleFactory;
//    }
//
//    public void setGlobal(final String identifier,
//                          final Object value) {
//        // Cannot set null values
//        if (value == null) {
//            return;
//        }
//
//        try {
//            //this.lock.lock();
//            // Make sure the global has been declared in the RuleBase
//            final Map globalDefintions = this.ruleBase.getGlobals();
//            final Class type = (Class) globalDefintions.get(identifier);
//            if ((type == null)) {
//                throw new RuntimeException("Unexpected global [" + identifier + "]");
//            } else if (!type.isInstance(value)) {
//                throw new RuntimeException("Illegal class for global. " + "Expected [" + type.getName() + "], " + "found [" + value.getClass().getName() + "].");
//            } else {
//                this.globalResolver.setGlobal(identifier,
//                    value);
//            }
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public void setGlobalResolver(final GlobalResolver globalResolver) {
//        try {
//            //this.lock.lock();
//            this.globalResolver = globalResolver;
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public GlobalResolver getGlobalResolver() {
//        return this.globalResolver;
//    }
//
//    public long getId() {
//        return this.id;
//    }
//
//    public Object getGlobal(final String identifier) {
//        try {
//            //this.lock.lock();
//            return this.globalResolver.resolveGlobal(identifier);
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public Agenda getAgenda() {
//        return this.agenda;
//    }
//
//    public void clearAgenda() {
//        this.agenda.clearAgenda();
//    }
//
//    public void clearAgendaGroup(final String group) {
//        this.agenda.clearAgendaGroup(group);
//    }
//
//    public void clearActivationGroup(final String group) {
//        this.agenda.clearActivationGroup(group);
//    }
//
//    public void clearRuleFlowGroup(final String group) {
//        this.agenda.clearRuleFlowGroup(group);
//    }
//
//    public RuleBase getRuleBase() {
//        return this.ruleBase;
//    }
//
//    public void halt() {
//        this.halt = true;
//    }
//
//    public void fireAllRules() throws FactException {
//        fireAllRules(null,
//            -1);
//    }
//
//    public void fireAllRules(int fireLimit) throws FactException {
//        fireAllRules(null,
//            fireLimit);
//    }
//
//    public void fireAllRules(final AgendaFilter agendaFilter) throws FactException {
//        fireAllRules(agendaFilter,
//            -1);
//    }
//
//    public void fireAllRules(final AgendaFilter agendaFilter,
//                             int fireLimit) throws FactException {
//        // If we're already firing a rule, then it'll pick up
//        // the firing for any other assertObject(..) that get
//        // nested inside, avoiding concurrent-modification
//        // exceptions, depending on code paths of the actions.
//        this.halt = false;
//
//        if (isSequential()) {
//            for (Iterator it = this.liaPropagations.iterator(); it.hasNext(); ) {
//                ((LIANodePropagation) it.next()).doPropagation(this);
//            }
//        }
//
//        if (!this.actionQueue.isEmpty()) {
//            executeQueuedActions();
//        }
//
//        boolean noneFired = true;
//
//        if (!this.firing) {
//            try {
//                this.firing = true;
//
//                while (continueFiring(fireLimit) && this.agenda.fireNextItem(agendaFilter)) {
//                    fireLimit = updateFireLimit(fireLimit);
//                    noneFired = false;
//                    if (!this.actionQueue.isEmpty()) {
//                        executeQueuedActions();
//                    }
//                }
//            } finally {
//                this.firing = false;
//                // @todo (mproctor) disabling Otherwise management for now, not happy with the current implementation
//                //                if ( noneFired ) {
//                //                    doOtherwise( agendaFilter,
//                //                                 fireLimit );
//                //                }
//
//            }
//        }
//    }
//
//    private final boolean continueFiring(final int fireLimit) {
//        return (!halt) && (fireLimit != 0);
//    }
//
//    private final int updateFireLimit(final int fireLimit) {
//        return fireLimit > 0 ? fireLimit - 1 : fireLimit;
//    }
//
//    /**
//     * This does the "otherwise" phase of processing.
//     * If no items are fired, then it will assert a temporary "Otherwise"
//     * fact and allow any rules to fire to handle "otherwise" cases.
//     */
//    private void doOtherwise(final AgendaFilter agendaFilter,
//                             int fireLimit) {
//        final FactHandle handle = this.insert(new Otherwise());
//        if (!this.actionQueue.isEmpty()) {
//            executeQueuedActions();
//        }
//
//        while (continueFiring(fireLimit) && this.agenda.fireNextItem(agendaFilter)) {
//            fireLimit = updateFireLimit(fireLimit);
//        }
//
//        this.retract(handle);
//    }
//
//    //
//    //        MN: The following is the traditional fireAllRules (without otherwise).
//    //            Purely kept here as this implementation of otherwise is still experimental.
//    //
//    //    public  void fireAllRules(final AgendaFilter agendaFilter) throws FactException {
//    //        // If we're already firing a rule, then it'll pick up
//    //        // the firing for any other assertObject(..) that get
//    //        // nested inside, avoiding concurrent-modification
//    //        // exceptions, depending on code paths of the actions.
//    //
//    //        if ( !this.factQueue.isEmpty() ) {
//    //            propagateQueuedActions();
//    //        }
//    //
//    //        if ( !this.firing ) {
//    //            try {
//    //                this.firing = true;
//    //
//    //                while ( this.agenda.fireNextItem( agendaFilter ) ) {
//    //                    ;
//    //                }
//    //            } finally {
//    //                this.firing = false;
//    //            }
//    //        }
//    //    }
//
//    /**
//     * Returns the fact Object for the given <code>FactHandle</code>. It
//     * actually attemps to return the value from the handle, before retrieving
//     * it from objects map.
//     *
//     * @param handle The <code>FactHandle</code> reference for the
//     *               <code>Object</code> lookup
//     * @see WorkingMemory
//     */
//    public Object getObject(final FactHandle handle) {
//        try {
//            //this.lock.lock();
//
//            // Make sure the FactHandle is from this WorkingMemory
//            final InternalFactHandle internalHandle = (InternalFactHandle) this.assertMap.get(handle);
//            if (internalHandle == null) {
//                return null;
//            }
//
//            Object object = internalHandle.getObject();
//
//            if (object != null && internalHandle.isShadowFact()) {
//                object = ((ShadowProxy) object).getShadowedObject();
//            }
//
//            return object;
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public ObjectHashMap getAssertMap() {
//        return this.assertMap;
//    }
//
//    /**
//     * @see WorkingMemory
//     */
//    public FactHandle getFactHandle(final Object object) {
//        try {
//            //this.lock.lock();
//            final FactHandle factHandle = (FactHandle) this.identityMap.get(object);
//
//            return factHandle;
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    /**
//     * This is an internal method, used to avoid java.util.Iterator adaptors
//     */
//    public ObjectHashMap getFactHandleMap() {
//        return this.assertMap;
//    }
//
//    /**
//     * This class is not thread safe, changes to the working memory during iteration may give unexpected results
//     */
//    public Iterator iterateObjects() {
//        HashTableIterator iterator = new HashTableIterator(this.assertMap);
//        iterator.reset();
//        return new JavaIteratorAdapter(iterator,
//            JavaIteratorAdapter.OBJECT);
//    }
//
//    /**
//     * This class is not thread safe, changes to the working memory during iteration may give unexpected results
//     */
//    public Iterator iterateObjects(ObjectFilter filter) {
//        HashTableIterator iterator = new HashTableIterator(this.assertMap);
//        iterator.reset();
//        return new JavaIteratorAdapter(iterator,
//            JavaIteratorAdapter.OBJECT,
//            filter);
//    }
//
//    /**
//     * This class is not thread safe, changes to the working memory during iteration may give unexpected results
//     */
//    public Iterator iterateFactHandles() {
//        HashTableIterator iterator = new HashTableIterator(this.assertMap);
//        iterator.reset();
//        return new JavaIteratorAdapter(iterator,
//            JavaIteratorAdapter.FACT_HANDLE);
//    }
//
//    /**
//     * This class is not thread safe, changes to the working memory during iteration may give unexpected results
//     */
//    public Iterator iterateFactHandles(ObjectFilter filter) {
//        HashTableIterator iterator = new HashTableIterator(this.assertMap);
//        iterator.reset();
//        return new JavaIteratorAdapter(iterator,
//            JavaIteratorAdapter.FACT_HANDLE,
//            filter);
//    }
//
//    public abstract QueryResults getQueryResults(String query);
//
//    public AgendaGroup getFocus() {
//        return this.agenda.getFocus();
//    }
//
//    public void setFocus(final String focus) {
//        this.agenda.setFocus(focus);
//    }
//
//    public void setFocus(final AgendaGroup focus) {
//        this.agenda.setFocus(focus);
//    }
//
//    public TruthMaintenanceSystem getTruthMaintenanceSystem() {
//        return this.tms;
//    }
//
//    /**
//     * @see WorkingMemory
//     */
//    public FactHandle insert(final Object object) throws FactException {
//        return insert(object, /* Not-Dynamic */
//            false,
//            false,
//            null,
//            null);
//    }
//
//    /**
//     * @see WorkingMemory
//     */
//    public FactHandle insertLogical(final Object object) throws FactException {
//        return insert(object, /* Not-Dynamic */
//            false,
//            true,
//            null,
//            null);
//    }
//
//    public FactHandle insert(final Object object,
//                             final boolean dynamic) throws FactException {
//        return insert(object,
//            dynamic,
//            false,
//            null,
//            null);
//    }
//
//    public FactHandle insertLogical(final Object object,
//                                    final boolean dynamic) throws FactException {
//        return insert(object,
//            dynamic,
//            true,
//            null,
//            null);
//    }
//
//    public FactHandle insert(final Object object,
//                             final boolean dynamic,
//                             boolean logical,
//                             final Rule rule,
//                             final Activation activation) throws FactException {
//        if (object == null) {
//            // you cannot assert a null object
//            return null;
//        }
//
//        InternalFactHandle handle = null;
//
//        if (isSequential()) {
//            handle = this.handleFactory.newFactHandle(object);
//            addHandleToMaps(handle);
//            insert(handle,
//                object,
//                rule,
//                activation);
//            return handle;
//        }
//
//        try {
//            //this.lock.lock();
//            // check if the object already exists in the WM
//            handle = (InternalFactHandle) this.assertMap.get(object);
//
//            if (this.maintainTms) {
//
//                EqualityKey key = null;
//
//                if (handle == null) {
//                    // lets see if the object is already logical asserted
//                    key = this.tms.get(object);
//                } else {
//                    // Object is already asserted, so check and possibly correct its
//                    // status and then return the handle
//                    key = handle.getEqualityKey();
//
//                    if (key.getStatus() == EqualityKey.STATED) {
//                        // return null as you cannot justify a stated object.
//                        return handle;
//                    }
//
//                    if (!logical) {
//                        // this object was previously justified, so we have to
//                        // override it to stated
//                        key.setStatus(EqualityKey.STATED);
//                        this.tms.removeLogicalDependencies(handle);
//                    } else {
//                        // this was object is already justified, so just add new
//                        // logical dependency
//                        this.tms.addLogicalDependency(handle,
//                            activation,
//                            activation.getPropagationContext(),
//                            rule);
//                    }
//
//                    return handle;
//                }
//
//                // At this point we know the handle is null
//                if (key == null) {
//                    // key is also null, so treat as a totally new stated/logical
//                    // assert
//                    handle = this.handleFactory.newFactHandle(object);
//                    addHandleToMaps(handle);
//
//                    key = new EqualityKey(handle);
//                    handle.setEqualityKey(key);
//                    this.tms.put(key);
//                    if (!logical) {
//                        key.setStatus(EqualityKey.STATED);
//                    } else {
//                        key.setStatus(EqualityKey.JUSTIFIED);
//                        this.tms.addLogicalDependency(handle,
//                            activation,
//                            activation.getPropagationContext(),
//                            rule);
//                    }
//                } else if (!logical) {
//                    if (key.getStatus() == EqualityKey.JUSTIFIED) {
//                        // Its previous justified, so switch to stated and remove
//                        // logical dependencies
//                        final InternalFactHandle justifiedHandle = key.getFactHandle();
//                        this.tms.removeLogicalDependencies(justifiedHandle);
//
//                        if (this.discardOnLogicalOverride) {
//                            // override, setting to new instance, and return
//                            // existing handle
//                            key.setStatus(EqualityKey.STATED);
//                            handle = key.getFactHandle();
//
//                            if (this.ruleBase.getConfiguration().getAssertBehaviour() == AssertBehaviour.IDENTITY) {
//                                // as assertMap may be using an "identity" equality comparator,
//                                // we need to remove the handle from the map, before replacing the object
//                                // and then re-add the handle. Otherwise we may end up with a leak.
//                                this.assertMap.remove(handle);
//                                Object oldObject = handle.getObject();
//                                if (oldObject instanceof ShadowProxy) {
//                                    ((ShadowProxy) oldObject).setShadowedObject(object);
//                                } else {
//                                    handle.setObject(object);
//                                }
//                                this.assertMap.put(handle,
//                                    handle,
//                                    false);
//                            } else {
//                                Object oldObject = handle.getObject();
//                                if (oldObject instanceof ShadowProxy) {
//                                    ((ShadowProxy) oldObject).setShadowedObject(object);
//                                } else {
//                                    handle.setObject(object);
//                                }
//                            }
//                            return handle;
//                        } else {
//                            // override, then instantiate new handle for assertion
//                            key.setStatus(EqualityKey.STATED);
//                            handle = this.handleFactory.newFactHandle(object);
//                            handle.setEqualityKey(key);
//                            key.addFactHandle(handle);
//                            addHandleToMaps(handle);
//                        }
//                    } else {
//                        handle = this.handleFactory.newFactHandle(object);
//                        addHandleToMaps(handle);
//                        key.addFactHandle(handle);
//                        handle.setEqualityKey(key);
//                    }
//                } else {
//                    if (key.getStatus() == EqualityKey.JUSTIFIED) {
//                        // only add as logical dependency if this wasn't previously
//                        // stated
//                        this.tms.addLogicalDependency(key.getFactHandle(),
//                            activation,
//                            activation.getPropagationContext(),
//                            rule);
//                        return key.getFactHandle();
//                    } else {
//                        // You cannot justify a previously stated equality equal
//                        // object, so return null
//                        return null;
//                    }
//                }
//            } else {
//                if (handle != null) {
//                    return handle;
//                }
//                handle = this.handleFactory.newFactHandle(object);
//                addHandleToMaps(handle);
//            }
//
//            if (dynamic) {
//                addPropertyChangeListener(object);
//            }
//
//            insert(handle,
//                object,
//                rule,
//                activation);
//        } finally {
//            //this.lock.unlock();
//        }
//        return handle;
//    }
//
//    protected void insert(InternalFactHandle handle,
//                          Object object,
//                          Rule rule,
//                          Activation activation) {
//        this.ruleBase.executeQueuedActions();
//
//        if (activation != null) {
//            // release resources so that they can be GC'ed
//            activation.getPropagationContext().releaseResources();
//        }
//        final PropagationContext propagationContext = new PropagationContextImpl(this.propagationIdCounter++,
//            PropagationContext.ASSERTION,
//            rule,
//            activation,
//            this.agenda.getActiveActivations(),
//            this.agenda.getDormantActivations());
//
//        doInsert(handle,
//            object,
//            propagationContext);
//
//        if (!this.actionQueue.isEmpty()) {
//            executeQueuedActions();
//        }
//
//        this.workingMemoryEventSupport.fireObjectInserted(propagationContext,
//            handle,
//            object,
//            this);
//    }
//
//    protected void addPropertyChangeListener(final Object object) {
//        try {
//            final Method method = object.getClass().getMethod("addPropertyChangeListener",
//                AbstractWorkingMemory.ADD_REMOVE_PROPERTY_CHANGE_LISTENER_ARG_TYPES);
//
//            method.invoke(object,
//                this.addRemovePropertyChangeListenerArgs);
//        } catch (final NoSuchMethodException e) {
//            System.err.println("Warning: Method addPropertyChangeListener not found" + " on the class " + object.getClass() + " so Drools" +
//                " will be unable to process JavaBean" + " PropertyChangeEvents on the asserted Object");
//        } catch (final IllegalArgumentException e) {
//            System.err.println("Warning: The addPropertyChangeListener method" + " on the class " + object.getClass() + " does not take" + " a simple PropertyChangeListener argument" + " so Drools will be unable to process JavaBean"
//                + " PropertyChangeEvents on the asserted Object");
//        } catch (final IllegalAccessException e) {
//            System.err.println("Warning: The addPropertyChangeListener method" + " on the class " + object.getClass() + " is not public" + " so Drools will be unable to process JavaBean" + " PropertyChangeEvents on the asserted Object");
//        } catch (final InvocationTargetException e) {
//            System.err.println("Warning: The addPropertyChangeListener method" + " on the class " + object.getClass() + " threw an " +
//                "InvocationTargetException" + " so Drools will be unable to process JavaBean"
//                + " PropertyChangeEvents on the asserted Object: " + e.getMessage());
//        } catch (final SecurityException e) {
//            System.err.println("Warning: The SecurityManager controlling the class " + object.getClass() + " did not allow the lookup of " +
//                "a" + " addPropertyChangeListener method" + " so Drools will be unable to process JavaBean"
//                + " PropertyChangeEvents on the asserted Object: " + e.getMessage());
//        }
//    }
//
//    public abstract void doInsert(InternalFactHandle factHandle,
//                                  Object object,
//                                  PropagationContext propagationContext) throws FactException;
//
//    protected void removePropertyChangeListener(final FactHandle handle) {
//        Object object = null;
//        try {
//            object = getObject(handle);
//
//            if (object != null) {
//                final Method mehod = object.getClass().getMethod("removePropertyChangeListener",
//                    AbstractWorkingMemory.ADD_REMOVE_PROPERTY_CHANGE_LISTENER_ARG_TYPES);
//
//                mehod.invoke(object,
//                    this.addRemovePropertyChangeListenerArgs);
//            }
//        } catch (final NoSuchMethodException e) {
//            // The removePropertyChangeListener method on the class
//            // was not found so Drools will be unable to
//            // stop processing JavaBean PropertyChangeEvents
//            // on the retracted Object
//        } catch (final IllegalArgumentException e) {
//            throw new RuntimeDroolsException("Warning: The removePropertyChangeListener method on the class " + object.getClass() + " " +
//                "does not take a simple PropertyChangeListener argument so Drools will be unable to stop processing JavaBean"
//                + " PropertyChangeEvents on the retracted Object");
//        } catch (final IllegalAccessException e) {
//            throw new RuntimeDroolsException("Warning: The removePropertyChangeListener method on the class " + object.getClass() + " is " +
//                "not public so Drools will be unable to stop processing JavaBean PropertyChangeEvents on the retracted Object");
//        } catch (final InvocationTargetException e) {
//            throw new RuntimeDroolsException("Warning: The removePropertyChangeL istener method on the class " + object.getClass() + " " +
//                "threw an InvocationTargetException so Drools will be unable to stop processing JavaBean"
//                + " PropertyChangeEvents on the retracted Object: " + e.getMessage());
//        } catch (final SecurityException e) {
//            throw new RuntimeDroolsException("Warning: The SecurityManager controlling the class " + object.getClass() + " did not allow " +
//                "the lookup of a removePropertyChangeListener method so Drools will be unable to stop processing JavaBean"
//                + " PropertyChangeEvents on the retracted Object: " + e.getMessage());
//        }
//    }
//
//    public void retract(final FactHandle handle) throws FactException {
//        retract(handle,
//            true,
//            true,
//            null,
//            null);
//    }
//
//    public abstract void doRetract(InternalFactHandle factHandle,
//                                   PropagationContext propagationContext);
//
//    /**
//     * @see WorkingMemory
//     */
//    public void retract(final FactHandle factHandle,
//                        final boolean removeLogical,
//                        final boolean updateEqualsMap,
//                        final Rule rule,
//                        final Activation activation) throws FactException {
//        try {
//            //this.lock.lock();
//            this.ruleBase.executeQueuedActions();
//
//            final InternalFactHandle handle = (InternalFactHandle) factHandle;
//            if (handle.getId() == -1) {
//                // can't retract an already retracted handle
//                return;
//            }
//            removePropertyChangeListener(handle);
//
//            if (activation != null) {
//                // release resources so that they can be GC'ed
//                activation.getPropagationContext().releaseResources();
//            }
//            final PropagationContext propagationContext = new PropagationContextImpl(this.propagationIdCounter++,
//                PropagationContext.RETRACTION,
//                rule,
//                activation,
//                this.agenda.getActiveActivations(),
//                this.agenda.getDormantActivations());
//
//            doRetract(handle,
//                propagationContext);
//
//            if (this.maintainTms) {
//                // Update the equality key, which maintains a list of stated
//                // FactHandles
//                final EqualityKey key = handle.getEqualityKey();
//
//                // Its justified so attempt to remove any logical dependencies for
//                // the handle
//                if (key.getStatus() == EqualityKey.JUSTIFIED) {
//                    this.tms.removeLogicalDependencies(handle);
//                }
//
//                key.removeFactHandle(handle);
//                handle.setEqualityKey(null);
//
//                // If the equality key is now empty, then remove it
//                if (key.isEmpty()) {
//                    this.tms.remove(key);
//                }
//            }
//
//            final Object object = handle.getObject();
//
//            this.workingMemoryEventSupport.fireObjectRetracted(propagationContext,
//                handle,
//                object,
//                this);
//
//            removeHandleFromMaps(handle);
//
//            this.handleFactory.destroyFactHandle(handle);
//
//            if (!this.actionQueue.isEmpty()) {
//                executeQueuedActions();
//            }
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    private void addHandleToMaps(InternalFactHandle handle) {
//        this.assertMap.put(handle,
//            handle,
//            false);
//        if (this.ruleBase.getConfiguration().getAssertBehaviour() == AssertBehaviour.EQUALITY) {
//            this.identityMap.put(handle,
//                handle,
//                false);
//        }
//    }
//
//    private void removeHandleFromMaps(final InternalFactHandle handle) {
//        this.assertMap.remove(handle);
//        if (this.ruleBase.getConfiguration().getAssertBehaviour() == AssertBehaviour.EQUALITY) {
//            this.identityMap.remove(handle);
//        }
//    }
//
//    public void modifyRetract(final FactHandle factHandle) {
//        modifyRetract(factHandle,
//            null,
//            null);
//    }
//
//    public void modifyRetract(final FactHandle factHandle,
//                              final Rule rule,
//                              final Activation activation) {
//        try {
//            //this.lock.lock();
//            this.ruleBase.executeQueuedActions();
//
//            // only needed if we maintain tms, but either way we must get it before we do the retract
//            int status = -1;
//            if (this.maintainTms) {
//                status = ((InternalFactHandle) factHandle).getEqualityKey().getStatus();
//            }
//            final InternalFactHandle handle = (InternalFactHandle) factHandle;
//            //final Object originalObject = (handle.isShadowFact()) ? ((ShadowProxy) handle.getObject()).getShadowedObject() : handle
//            // .getObject();
//
//            if (handle.getId() == -1) {
//                // the handle is invalid, most likely already  retracted, so return
//                return;
//            }
//
//            if (activation != null) {
//                // release resources so that they can be GC'ed
//                activation.getPropagationContext().releaseResources();
//            }
//            // Nowretract any trace  of the original fact
//            final PropagationContext propagationContext = new PropagationContextImpl(this.propagationIdCounter++,
//                PropagationContext.MODIFICATION,
//                rule,
//                activation,
//                this.agenda.getActiveActivations(),
//                this.agenda.getDormantActivations());
//            doRetract(handle,
//                propagationContext);
//
//            if (this.maintainTms) {
//
//                // the hashCode and equality has changed, so we must update the EqualityKey
//                EqualityKey key = handle.getEqualityKey();
//                key.removeFactHandle(handle);
//
//                // If the equality key is now empty, then remove it
//                if (key.isEmpty()) {
//                    this.tms.remove(key);
//                }
//            }
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public void modifyInsert(final FactHandle factHandle,
//                             final Object object) {
//        modifyInsert(factHandle,
//            object,
//            null,
//            null);
//    }
//
//    public void modifyInsert(final FactHandle factHandle,
//                             final Object object,
//                             final Rule rule,
//                             final Activation activation) {
//        try {
//            //this.lock.lock();
//            this.ruleBase.executeQueuedActions();
//
//            final InternalFactHandle handle = (InternalFactHandle) factHandle;
//            final Object originalObject =
//                (handle.isShadowFact()) ? ((ShadowProxy) handle.getObject()).getShadowedObject() : handle.getObject();
//
//            if (this.maintainTms) {
//                EqualityKey key = handle.getEqualityKey();
//
//                // now use an  existing  EqualityKey, if it exists, else create a new one
//                key = this.tms.get(object);
//                if (key == null) {
//                    key = new EqualityKey(handle,
//                        0);
//                    this.tms.put(key);
//                } else {
//                    key.addFactHandle(handle);
//                }
//
//                handle.setEqualityKey(key);
//            }
//
//            this.handleFactory.increaseFactHandleRecency(handle);
//
//            if (activation != null) {
//                // release resources so that they can be GC'ed
//                activation.getPropagationContext().releaseResources();
//            }
//            // Nowretract any trace  of the original fact
//            final PropagationContext propagationContext = new PropagationContextImpl(this.propagationIdCounter++,
//                PropagationContext.MODIFICATION,
//                rule,
//                activation,
//                this.agenda.getActiveActivations(),
//                this.agenda.getDormantActivations());
//
//            doInsert(handle,
//                object,
//                propagationContext);
//
//            this.workingMemoryEventSupport.fireObjectUpdated(propagationContext,
//                factHandle,
//                originalObject,
//                object,
//                this);
//
//            propagationContext.clearRetractedTuples();
//
//            if (!this.actionQueue.isEmpty()) {
//                executeQueuedActions();
//            }
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public void update(final FactHandle handle,
//                       final Object object) throws FactException {
//        update(handle,
//            object,
//            null,
//            null);
//    }
//
//    /**
//     * modify is implemented as half way retract / assert due to the truth
//     * maintenance issues.
//     *
//     * @see WorkingMemory
//     */
//    public void update(final FactHandle factHandle,
//                       final Object object,
//                       final Rule rule,
//                       final Activation activation) throws FactException {
//        try {
//            //this.lock.lock();
//            this.ruleBase.executeQueuedActions();
//
//            // only needed if we maintain tms, but either way we must get it before we do the retract
//            int status = -1;
//            if (this.maintainTms) {
//                status = ((InternalFactHandle) factHandle).getEqualityKey().getStatus();
//            }
//            final InternalFactHandle handle = (InternalFactHandle) factHandle;
//            final Object originalObject =
//                (handle.isShadowFact()) ? ((ShadowProxy) handle.getObject()).getShadowedObject() : handle.getObject();
//
//            if (handle.getId() == -1 || object == null) {
//                // the handle is invalid, most likely already  retracted, so return
//                // and we cannot assert a null object
//                return;
//            }
//
//            if (activation != null) {
//                // release resources so that they can be GC'ed
//                activation.getPropagationContext().releaseResources();
//            }
//            // Nowretract any trace  of the original fact
//            final PropagationContext propagationContext = new PropagationContextImpl(this.propagationIdCounter++,
//                PropagationContext.MODIFICATION,
//                rule,
//                activation,
//                this.agenda.getActiveActivations(),
//                this.agenda.getDormantActivations());
//            doRetract(handle,
//                propagationContext);
//
//            if ((originalObject != object) || (this.ruleBase.getConfiguration().getAssertBehaviour() != AssertBehaviour.IDENTITY)) {
//                removeHandleFromMaps(handle);
//
//                // set anyway, so that it updates the hashCodes
//                handle.setObject(object);
//                addHandleToMaps(handle);
//            }
//
//            if (this.maintainTms) {
//
//                // the hashCode and equality has changed, so we must update the EqualityKey
//                EqualityKey key = handle.getEqualityKey();
//                key.removeFactHandle(handle);
//
//                // If the equality key is now empty, then remove it
//                if (key.isEmpty()) {
//                    this.tms.remove(key);
//                }
//
//                // now use an  existing  EqualityKey, if it exists, else create a new one
//                key = this.tms.get(object);
//                if (key == null) {
//                    key = new EqualityKey(handle,
//                        status);
//                    this.tms.put(key);
//                } else {
//                    key.addFactHandle(handle);
//                }
//
//                handle.setEqualityKey(key);
//            }
//
//            this.handleFactory.increaseFactHandleRecency(handle);
//
//            doInsert(handle,
//                object,
//                propagationContext);
//
//            this.workingMemoryEventSupport.fireObjectUpdated(propagationContext,
//                factHandle,
//                originalObject,
//                object,
//                this);
//
//            propagationContext.clearRetractedTuples();
//
//            if (!this.actionQueue.isEmpty()) {
//                executeQueuedActions();
//            }
//        } finally {
//            //this.lock.unlock();
//        }
//    }
//
//    public void executeQueuedActions() {
//        while (!actionQueue.isEmpty()) {
//            final WorkingMemoryAction action = (WorkingMemoryAction) actionQueue.get(0);
//            actionQueue.remove(0);
//            action.execute(this);
//        }
//        //        for ( final Iterator it = this.actionQueue.iterator(); it.hasNext(); ) {
//        //            final WorkingMemoryAction action = (WorkingMemoryAction) it.next();
//        //            it.remove();
//        //            action.execute( this );
//        //        }
//    }
//
//    public void queueWorkingMemoryAction(final WorkingMemoryAction action) {
//        this.actionQueue.add(action);
//    }
//
//    public void removeLogicalDependencies(final Activation activation,
//                                          final PropagationContext context,
//                                          final Rule rule) throws FactException {
//        if (this.maintainTms) {
//            this.tms.removeLogicalDependencies(activation,
//                context,
//                rule);
//        }
//    }
//
//    /**
//     * Retrieve the <code>JoinMemory</code> for a particular
//     * <code>JoinNode</code>.
//     *
//     * @param node The <code>JoinNode</code> key.
//     * @return The node's memory.
//     */
//    public Object getNodeMemory(final NodeMemory node) {
//        Object memory = this.nodeMemories.get(node.getId());
//
//        if (memory == null) {
//            memory = node.createMemory(this.ruleBase.getConfiguration());
//
//            this.nodeMemories.put(node.getId(),
//                memory);
//        }
//
//        return memory;
//    }
//
//    public void clearNodeMemory(final NodeMemory node) {
//        this.nodeMemories.remove(node.getId());
//    }
//
//    public WorkingMemoryEventSupport getWorkingMemoryEventSupport() {
//        return this.workingMemoryEventSupport;
//    }
//
//    public AgendaEventSupport getAgendaEventSupport() {
//        return this.agendaEventSupport;
//    }
//
//    public RuleFlowEventSupport getRuleFlowEventSupport() {
//        return this.ruleFlowEventSupport;
//    }
//
//    /**
//     * Sets the AsyncExceptionHandler to handle exceptions thrown by the Agenda
//     * Scheduler used for duration rules.
//     *
//     * @param handler
//     */
//    public void setAsyncExceptionHandler(final AsyncExceptionHandler handler) {
//        // this.agenda.setAsyncExceptionHandler( handler );
//    }
//
//    /*
//     * public void dumpMemory() { Iterator it = this.joinMemories.keySet(
//     * ).iterator( ); while ( it.hasNext( ) ) { ((JoinMemory)
//     * this.joinMemories.get( it.next( ) )).dump( ); } }
//     */
//
//    public void propertyChange(final PropertyChangeEvent event) {
//        final Object object = event.getSource();
//
//        try {
//            FactHandle handle = getFactHandle(object);
//            if (handle == null) {
//                throw new FactException("Update error: handle not found for object: " + object + ". Is it in the working memory?");
//            }
//            update(handle,
//                object);
//        } catch (final FactException e) {
//            throw new RuntimeDroolsException(e.getMessage());
//        }
//    }
//
//    public long getNextPropagationIdCounter() {
//        return this.propagationIdCounter++;
//    }
//
//    public Lock getLock() {
//        return this.lock;
//    }
//
//    public class RuleFlowDeactivateEvent {
//
//        public void propagate() {
//
//        }
//    }
//
//    public ProcessInstance startProcess(final String processId) {
//        final Process process = ((InternalRuleBase) getRuleBase()).getProcess(processId);
//        if (process == null) {
//            throw new IllegalArgumentException("Unknown process ID: " + processId);
//        }
//        if (process instanceof RuleFlowProcess) {
//            final RuleFlowProcessInstance processInstance = new RuleFlowProcessInstanceImpl();
//            processInstance.setWorkingMemory(this);
//            processInstance.setProcess(process);
//            processInstance.setId(++processCounter);
//            processInstance.start();
//
//            getRuleFlowEventSupport().fireRuleFlowProcessStarted(processInstance,
//                this);
//
//            return processInstance;
//        } else {
//            throw new IllegalArgumentException("Unknown process type: " + process.getClass());
//        }
//    }
//
//    public List iterateObjectsToList() {
//        List result = new ArrayList();
//        Iterator iterator = iterateObjects();
//        for (; iterator.hasNext(); ) {
//            result.add(iterator.next());
//        }
//        return result;
//    }
//
//    public Entry[] getActivationParameters(long activationId) {
//        Activation[] activations = getAgenda().getActivations();
//        for (int i = 0; i < activations.length; i++) {
//            if (activations[i].getActivationNumber() == activationId) {
//                Map params = getActivationParameters(activations[i]);
//                return (Entry[]) params.entrySet().toArray(new Entry[params.size()]);
//            }
//        }
//        return new Entry[0];
//    }
//
//    /**
//     * Helper method
//     */
//    public Map getActivationParameters(Activation activation) {
//        Map result = new HashMap();
//        Declaration[] declarations = activation.getRule().getDeclarations();
//        for (int i = 0; i < declarations.length; i++) {
//            FactHandle handle = activation.getTuple().get(declarations[i]);
//            if (handle instanceof InternalFactHandle) {
//                result.put(declarations[i].getIdentifier(),
//                    declarations[i].getValue(this,
//                        ((InternalFactHandle) handle).getObject()));
//            }
//        }
//        return result;
//    }
//}
