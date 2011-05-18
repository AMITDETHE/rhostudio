package rhogenwizard.debugger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class DebugProtocol {
	private DebugServer debugServer;
	private IDebugCallback debugCallback;
	private DebugState state;
	private String filePosition = "";
	private int linePosition = 0;
	private String classPosition = "";
	private String methodPosition = "";
	
	public DebugProtocol (DebugServer server, IDebugCallback callback) {
		this.debugServer = server;
		this.debugCallback = callback;
		this.state = DebugState.NOTCONNECTED;
	}

	public DebugState getState() {
		return this.state;
	}

	public String getCurrentFile() {
		return this.filePosition;
	}

	public int getCurrentLine() {
		return this.linePosition;
	}
	
	public String getCurrentClass() {
		return this.classPosition;
	}

	public String getCurrentMethod() {
		return this.methodPosition;
	}
	
	protected void processCommand(String cmd) {
		boolean bp=false, stInto=false, stOver=false, stRet=false;
		if(cmd.endsWith("\n"))
			cmd = cmd.substring(0, cmd.length()-1);
		if (cmd.compareTo("CONNECT")==0) {
			this.state = DebugState.CONNECTED;
			debugServer.send("CONNECTED");
			debugCallback.connected();
		} else if (cmd.compareTo("RESUMED")==0) {
			this.state = DebugState.RUNNING;
			debugCallback.resumed();
		} else if (cmd.compareTo("QUIT")==0) {
			this.state = DebugState.EXITED;
			debugCallback.exited();
		} else if (
			(bp=cmd.startsWith("BP:")) ||
			(stInto=cmd.startsWith("STEP:")) ||
			(stOver=cmd.startsWith("STOVER:")) ||
			(stRet=cmd.startsWith("STRET:")) ||
			cmd.startsWith("SUSP:"))
		{
			this.state = bp ? DebugState.BREAKPOINT
					: (stInto ? DebugState.STOPPED_INTO
							: (stOver ? DebugState.STOPPED_OVER
									: (stRet ? DebugState.STOPPED_RETURN
											: DebugState.SUSPENDED)));
			String[] brp = cmd.split(":");
			this.filePosition = brp[1].replace('|', ':').replace('\\', '/');
			this.linePosition = Integer.parseInt(brp[2]);
			this.classPosition = brp.length > 3 ? brp[3].replace('#', ':') : "";
			this.methodPosition = brp.length > 4 ? brp[4] : "";
			debugCallback.stopped(this.state, this.filePosition, this.linePosition, this.classPosition, this.methodPosition);
		} else if (cmd.startsWith("EVL:")) {
			boolean valid = cmd.charAt(4)=='0';
			String var = cmd.substring(6);
			String val = "";
			int val_idx = var.indexOf(':');
			if (val_idx>=0) {
				val = var.substring(val_idx+1); // .replace("\\n", "\n");
				try {
					var = URLDecoder.decode(var.substring(0,val_idx), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					var = var.substring(0,val_idx);
				}
			}
			debugCallback.evaluation(valid, var, val);
		} else if (cmd.startsWith("V:")) {
			DebugVariableType vt = DebugVariableType.variableTypeById(cmd.charAt(2));
			String var = cmd.substring(4);
			String val = "";
			int val_idx = var.indexOf(':');
			if (val_idx>=0) {
				val = var.substring(val_idx+1); // .replace("\\n", "\n");
				var = var.substring(0,val_idx);
			}
			debugCallback.watch(vt, var, val);
		} else if (cmd.startsWith("VSTART:")) {
			debugCallback.watchBOL(DebugVariableType.variableTypeById(cmd.charAt(7)));
		} else if (cmd.startsWith("VEND:")) {
			debugCallback.watchEOL(DebugVariableType.variableTypeById(cmd.charAt(5)));
		} else {
			debugCallback.unknown(cmd);
		}
	}

	public void stepOver() {
		this.state = DebugState.RUNNING;
		debugServer.send("STEPOVER");
	}

	public void stepInto() {
		this.state = DebugState.RUNNING;
		debugServer.send("STEPINTO");
	}

	public void stepReturn() {
		this.state = DebugState.RUNNING;
		debugServer.send("STEPRET");
	}
	
	public void resume() {
		this.state = DebugState.RUNNING;
		debugServer.send("CONT");
	}
	
	public void addBreakpoint(String file, int line) {
		debugServer.send("BP:"+file+":"+line);
	}

	public void removeBreakpoint(String file, int line) {
		debugServer.send("RM:"+file+":"+line);
	}

	public void removeAllBreakpoints() {
		debugServer.send("RMALL");
	}
	
	public void skipBreakpoints(boolean skip) {
		debugServer.send(skip?"DISABLE":"ENABLE");
	}
	
	public void evaluate(String expression) {
		try {
			expression = URLEncoder.encode(expression, "UTF-8");
		} catch (UnsupportedEncodingException e) {}
		debugServer.send("EVL:"+expression);
	}

	public void getVariables(DebugVariableType[] types) {
		for (DebugVariableType t: types) {
			switch (t) {
			case GLOBAL:
				debugServer.send("GVARS"); break;
			case CLASS:
				debugServer.send("CVARS"); break;
			case INSTANCE:
				debugServer.send("IVARS"); break;
			default:
				debugServer.send("LVARS");
			}
		}
	}

	public void suspend() {
		debugServer.send("SUSP");
	}

	public void terminate() {
		debugServer.send("KILL");
	}
}
