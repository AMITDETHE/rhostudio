package rhogenwizard.debugger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Rhodes Debug Server implementation.
 * @author Albert R. Timashev
 */
public class DebugServer extends Thread {
	static private PrintStream debugOutput = null;
	
	private int debugServerPort = 9000;
	private IDebugCallback debugCallback;
	private DebugProtocol debugProtocol = null;

	private ServerSocket serverSocket = null;
	private BufferedReader inFromClient = null;
	private OutputStreamWriter outToClient = null;

	/**
	 * Create a debug server on default port (9000).
	 * @param callback - object to receive events from the debug target (Rhodes application).
	 */
	public DebugServer(IDebugCallback callback) {
		this.debugCallback = callback;
		this.initialize();
	}

	/**
	 * Create a debug server on a specified port.
	 * @param callback - object to receive events from the debug target (Rhodes application).
	 * @param port - server port to bind/listen to.
	 */
	public DebugServer(IDebugCallback callback, int port) {
		this.debugServerPort = port;
		this.debugCallback = callback;
		this.initialize();
	}

	/**
	 * Set an output stream for a detailed debug information.
	 * @param stream - output stream (if null, no debug information will be passed anywhere). 
	 */
	public static void setDebugOutputStream(PrintStream stream) {
		debugOutput = stream;
	}
	
	/**
	 * Get the debug server port.
	 * @return Port number the debug server is bound/listening to. 
	 */
	public int getPort() {
		return this.debugServerPort;
	}

	private void initialize() {
		try {
			this.serverSocket = new java.net.ServerSocket(this.debugServerPort);
			assert this.serverSocket.isBound();
			if ((debugOutput != null) && this.serverSocket.isBound()) {
				debugOutput.println("Debug server port "
					+ this.serverSocket.getLocalPort()
					+ " is ready and waiting for Rhodes application to connect...");
			}
		} catch (SocketException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void run() {
		try {
			Socket clientSocket = serverSocket.accept();
			inFromClient = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));
			outToClient = new OutputStreamWriter(new BufferedOutputStream(
				clientSocket.getOutputStream()), "US-ASCII");
			debugProtocol = new DebugProtocol(this, this.debugCallback);
			try {
				String data;
				while ((data = inFromClient.readLine()) != null) {
					if (debugOutput != null)
						debugOutput.println("Received: " + data);
					debugProtocol.processCommand(data);
				}
			} catch (EOFException e) {
			} catch (IOException e) {
			} finally {
				try {
					clientSocket.close();
				} catch (IOException e) {
				}
			}
		} catch (IOException ioe) {
		}
	}

	/**
	 * Shutdown the debug server (close all connections and a server socket).
	 */
	public void shutdown() {
		this.interrupt();
		try {
			if (this.inFromClient != null)
				this.inFromClient.close();
			if (this.outToClient != null)
				this.outToClient.close();
			if (this.serverSocket != null)
				this.serverSocket.close();
		} catch (IOException ioe) {
		}
		this.inFromClient = null;
		this.outToClient = null;
		this.serverSocket = null;
		if (debugOutput != null)
			debugOutput.println("Debug server stopped.");
	}

	protected void send(String cmd) {
		try {
			if (!cmd.endsWith("\n"))
				cmd += "\n";
			outToClient.write(cmd);
			outToClient.flush();
		} catch (IOException ioe) {
		}
	}

	/**
	 * Get current state of the connected Rhodes application.
	 * @return Returns a {@link DebugState}.
	 */
	public DebugState debugGetState() {
		return this.debugProtocol!=null ? this.debugProtocol.getState() : DebugState.NOTCONNECTED;
	}

	/**
	 * Step over the next method call (without entering it) at the currently executing line of Ruby code.
	 */
	public void debugStepOver() {
		if (this.debugProtocol!=null)
			this.debugProtocol.stepOver();
	}
	
	/**
	 * Step into the next method call at the currently executing line of Ruby code.
	 */
	public void debugStepInto() {
		if (this.debugProtocol!=null)
			this.debugProtocol.stepInto();
	}
	
	/**
	 * Resume a normal execution of the Rhodes application (after the stop at breakpoint or after {@link #debugStep()} method call). 
	 */
	public void debugResume() {
		if (this.debugProtocol!=null)
			this.debugProtocol.resume();
	}
	
	/**
	 * Add a breakpoint.
	 * @param file - file path within <code>app</code> folder of the Rhodes application,
	 * e.g. <code>"application.rb"</code>
	 * (always use <code>'/'</code> as a folder/file name separator).
	 * @param line - effective line number (starting with 1). Must point to non-empty line of code.
	 */
	public void debugBreakpoint(String file, int line) {
		if (this.debugProtocol!=null)
			this.debugProtocol.addBreakpoint(file, line);
	}

	/**
	 * Remove a breakpoint.
	 * @param file - file path within <code>app</code> folder of the Rhodes application,
	 * e.g. <code>"application.rb"</code>
	 * (always use <code>'/'</code> as a folder/file name separator).
	 * @param line - effective line number (starting with 1). Must point to non-empty line of code.
	 */
	public void debugRemoveBreakpoint(String file, int line) {
		if (this.debugProtocol!=null)
			this.debugProtocol.removeBreakpoint(file, line);
	}

	/**
	 * Remove all breakpoints.
	 */
	public void debugRemoveAllBreakpoints() {
		if (this.debugProtocol!=null)
			this.debugProtocol.removeAllBreakpoints();
	}

	/**
	 * Toggle breakpoints skip mode.
	 * @param skip - if <code>true</code>, skip all breakpoints; if <code>false</code>, stop at breakpoints.
	 */
	public void debugSkipBreakpoints(boolean skip) {
		if (this.debugProtocol!=null)
			this.debugProtocol.skipBreakpoints(skip);
	}

	/**
	 * Evaluate Ruby expression or execute arbitrary Ruby code. 
	 * @param expression - expression to evaluate or Ruby code to execute.
	 * Result of evaluation/execution is returned by the
	 * {@link IDebugCallback#evaluation(String)} method call.  
	 */
	public void debugEvaluate(String expression) {
		if (this.debugProtocol!=null)
			this.debugProtocol.evaluate(expression);
	}

}
