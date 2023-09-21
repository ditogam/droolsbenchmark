/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */
package org.drools.util.concurrent.locks;

import java.util.Collection;

/**
 * This is a stripped down version of jdk1.5 ReentrantLock.
 * All the condition and wait stuff has been removed
 *
 * @author Doug Lea
 * @author Dawid Kurzyniec
 * @since 1.5
 */
public class ReentrantLock
//    extends java.util.concurrent.locks.ReentrantLock
    implements
    Lock,
    java.io.Serializable {
    private static final long serialVersionUID = 400L;

    /**
     * Creates an instance of <tt>ReentrantLock</tt>.
     * This is equivalent to using <tt>ReentrantLock(false)</tt>.
     */
    public ReentrantLock() {
        super();
//        java.util.concurrent.locks.ReentrantLock.isHeldByCurrentThread
    }

    @Override
    public void lock() {

    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return true;
    }

    @Override
    public void unlock() {

    }

    public boolean isHeldByCurrentThread() {
        return false;
    }
}
