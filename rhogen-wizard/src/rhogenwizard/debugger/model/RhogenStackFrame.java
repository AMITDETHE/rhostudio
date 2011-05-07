/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *******************************************************************************/
package rhogenwizard.debugger.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

public class RhogenStackFrame extends RhogenDebugElement implements IStackFrame 
{
	private RhogenThread fThread = null;
	private String fName = null;
	private int fPC;
	private String fFileName = null;
	private int fId;
	private List<IVariable> fVariables;
	
	/**
	 * Constructs a stack frame in the given thread with the given
	 * frame data.
	 * 
	 * @param thread
	 * @param data frame data
	 * @param id stack frame id (0 is the bottom of the stack)
	 */
	public RhogenStackFrame(RhogenThread thread, StackData stackData, int id) 
	{
		super((RhogenDebugTarget) thread.getDebugTarget());
		
		fId     = id;
		fThread = thread;
		init(stackData);
	}
	
	/**
	 * Initializes this frame based on its data
	 * 
	 * @param data
	 */
	private void init(StackData stackData)
	{
		fFileName   = stackData.m_resName; 
		fPC         = stackData.m_codeLine; 
		fName       = stackData.m_resName; 
		fVariables  = new ArrayList<IVariable>();
	}
	
	public void addVariables(IVariable variable)
	{
		fVariables.add(variable);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getThread()
	 */
	public IThread getThread() 
	{
		return fThread;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException 
	{
		IVariable[] array = new IVariable[fVariables.size()];

		array = fVariables.toArray(array);
		
		return array;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#hasVariables()
	 */
	public boolean hasVariables() throws DebugException 
	{
		return fVariables.size() > 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getLineNumber()
	 */
	public int getLineNumber() throws DebugException 
	{
		return fPC;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getCharStart()
	 */
	public int getCharStart() throws DebugException 
	{
		return -1;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getCharEnd()
	 */
	public int getCharEnd() throws DebugException 
	{
		return -1;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getName()
	 */
	public String getName() throws DebugException 
	{
		return fName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getRegisterGroups()
	 */
	public IRegisterGroup[] getRegisterGroups() throws DebugException
	{
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#hasRegisterGroups()
	 */
	public boolean hasRegisterGroups() throws DebugException {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepInto()
	 */
	public boolean canStepInto() {
		return getThread().canStepInto();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepOver()
	 */
	public boolean canStepOver() {
		return getThread().canStepOver();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepReturn()
	 */
	public boolean canStepReturn() {
		return getThread().canStepReturn();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#isStepping()
	 */
	public boolean isStepping() {
		return getThread().isStepping();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepInto()
	 */
	public void stepInto() throws DebugException {
		getThread().stepInto();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public void stepOver() throws DebugException {
		getThread().stepOver();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException {
		getThread().stepReturn();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return getThread().canResume();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return getThread().canSuspend();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return getThread().isSuspended();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		getThread().resume();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		getThread().suspend();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return getThread().canTerminate();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() 
	{
		return getThread().isTerminated();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException 
	{
		getThread().terminate();
	}
	
	/**
	 * Returns the name of the source file this stack frame is associated
	 * with.
	 * 
	 * @return the name of the source file this stack frame is associated
	 * with
	 */
	public String getSourceName() 
	{
		return fFileName;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) 
	{
		if (obj instanceof RhogenStackFrame) 
		{
			RhogenStackFrame sf = (RhogenStackFrame)obj;
			
			try 
			{
				return sf.getSourceName().equals(getSourceName()) &&
					sf.getLineNumber() == getLineNumber() &&
					sf.fId == fId;
			} 
			catch (DebugException e) 
			{
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() 
	{
		return getSourceName().hashCode() + fId;
	}
	
	/**
	 * Returns this stack frame's unique identifier within its thread
	 * 
	 * @return this stack frame's unique identifier within its thread
	 */
	protected int getIdentifier() 
	{
		return fId;
	}
}
