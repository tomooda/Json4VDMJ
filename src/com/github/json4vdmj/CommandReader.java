/*******************************************************************************
 *
 *	Copyright (C) 2008 Fujitsu Services Ltd.
 *
 *	Author: Nick Battle
 *
 *	This file is part of VDMJ.
 *
 *	VDMJ is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	VDMJ is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with VDMJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

/*******************************************************************************
 * Modified and Merged to Json4VDMJ by Tomohiro Oda, 2015
 *
 *	Json4VDMJ is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Json4VDMJ is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with Json4VDMJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package com.github.json4vdmj;

import net.arnx.jsonic.JSON;
import org.overturetool.vdmj.ExitStatus;
import org.overturetool.vdmj.Release;
import org.overturetool.vdmj.Settings;
import org.overturetool.vdmj.lex.Dialect;
import org.overturetool.vdmj.lex.LexLocation;
import org.overturetool.vdmj.lex.LexNameToken;
import org.overturetool.vdmj.lex.LexTokenReader;
import org.overturetool.vdmj.messages.Console;
import org.overturetool.vdmj.messages.VDMErrorsException;
import org.overturetool.vdmj.modules.Module;
import org.overturetool.vdmj.runtime.Context;
import org.overturetool.vdmj.runtime.DebuggerException;
import org.overturetool.vdmj.runtime.ModuleInterpreter;
import org.overturetool.vdmj.runtime.StateContext;
import org.overturetool.vdmj.statements.Statement;
import org.overturetool.vdmj.syntax.ParserException;
import org.overturetool.vdmj.syntax.StatementReader;
import org.overturetool.vdmj.typechecker.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tomohiro on 15/04/14.
 */
public class CommandReader extends org.overturetool.vdmj.commands.CommandReader {
    private final ModuleInterpreter interpreter;
    public CommandReader(ModuleInterpreter interp) {
        super (interp, "");
        this.interpreter = interp;
    }

    /* the main REPL loop */
    public ExitStatus run(List<File> filenames)
    {
        String line = "";
        boolean carryOn = true;

        while (carryOn)
        {

            try
            {
                print(prompt);
                line = getStdin().readLine();

                if (line == null || line.equals("quit") || line.equals("q"))
                {
                    System.exit(0);
                }
                else if (line.startsWith("load"))
                {
                    carryOn = doLoad(line, filenames);

                    if (!carryOn) {
                        return ExitStatus.RELOAD;
                    }
                }
                else if (line.equals("init"))
                {
                    carryOn = doInit(line);
                }
                else if(line.equals("modules"))
                {
                    carryOn = doModules(line);
                }
                else if(line.equals("state"))
                {
                    carryOn = doState(line);
                }
                else if(line.startsWith("default"))
                {
                    carryOn = doDefault(line);
                }
                else if (line.startsWith("print ") || line.startsWith("p "))
                {
                    carryOn = doEvaluate(line);
                }
                else if (line.startsWith("statement ") || line.startsWith("s "))
                {
                    carryOn = doStatement(line);
                }
                else if (line.equals("classic"))
                {
                    Settings.release = Release.CLASSIC;
                    printOK("CLASSIC");
                    carryOn = true;
                }
                else if (line.equals("vdm10"))
                {
                    Settings.release = Release.VDM_10;
                    printOK("VDM10");
                    carryOn = true;
                }
                else if (line.equals("-rtc"))
                {
                    Settings.prechecks = false;
                    Settings.postchecks = false;
                    Settings.invchecks = false;
                    Settings.dynamictypechecks = false;
                    printOK("rtc off");
                    carryOn = true;
                }
                else if (line.equals("+rtc"))
                {
                    Settings.prechecks = true;
                    Settings.postchecks = true;
                    Settings.invchecks = true;
                    Settings.dynamictypechecks = true;
                    printOK("rtc on");
                    carryOn = true;
                }
            }
            catch (Exception e)
            {
                carryOn = doException(e);
            }
        }

        return ExitStatus.EXIT_OK;
    }

    @Override
    protected boolean doLoad(String line, List<File> filenames)
    {
        int spacePos = line.indexOf(' ');
        if (spacePos < 0)
        {
            printError("Usage: load <files or dirs>");
            return true;
        }

        filenames.clear();
        filenames.add(new File(line.substring(spacePos + 1)));
        return false;
    }

    @Override
    protected boolean doInit(@SuppressWarnings("unused") String line)
    {
        LexLocation.clearLocations();
        interpreter.init(null);
        printOK("");
        return true;
    }

    @Override
    protected boolean doModules(String line)
    {
        List<Module> modules = interpreter.getModules();
        List<String> names = new ArrayList<String>(modules.size());
        for (Module m: modules)
        {
            names.add(m.name.name);
        }
        printOK(names);
        return true;
    }

    @Override
    protected boolean doState(String line)
    {
        Context context = interpreter.getStateContext();
        if (context == null) {
            printOK(new Object[0]);
        } else {
            HashMap<String,String> binds = new HashMap<String,String>();
            for (LexNameToken var : context.keySet()) {
                binds.put(var.name, context.get(var).toShortString(1024*1024));
            }
            printOK(binds);
        }
        return true;
    }

    @Override
    protected boolean doDefault(String line) throws Exception
    {
        String parts[] = line.split("\\s+");

        if (parts.length != 2)
        {
            throw new Exception("Usage: default <default module name>");
        }

        try {
            interpreter.setDefaultName(parts[1]);
        } catch (Exception e) {}
        printOK(interpreter.getDefaultName());
        return true;
    }

    @Override
    protected boolean doEvaluate(String line)
    {
        line = line.substring(line.indexOf(' ') + 1);

        try
        {
            printOK(interpreter.execute(line, null).toShortString(1024*1024));
        }
        catch (ParserException e)
        {
            printError("Syntax: " + e.getMessage());
        }
        catch (DebuggerException e)
        {
            printError("Debug: " + e.getMessage());
        }
        catch (RuntimeException e)
        {
            printError("Runtime: " + e);
        }
        catch (VDMErrorsException e)
        {
            printError(e.toString());
        }
        catch (Exception e)
        {
            printError("Error: " + e.getMessage());
        }

        return true;
    }

    protected boolean doStatement(String line)
    {
        line = line.substring(line.indexOf(' ') + 1);

        try
        {
            Module defaultModule = interpreter.defaultModule;
            LexTokenReader ltr = new LexTokenReader(line, Dialect.VDM_SL, Console.charset);
            StatementReader reader = new StatementReader(ltr);
            reader.setCurrentModule(interpreter.getDefaultName());
            Statement statement = reader.readStatement();
            Environment env = interpreter.getGlobalEnvironment();
            interpreter.typeCheck(statement, env);
            Context mainContext = new StateContext(defaultModule.name.location,
                    "module scope",	null, defaultModule.getStateContext());

            mainContext.putAll(interpreter.initialContext);
            mainContext.setThreadState(null, null);
            interpreter.clearBreakpointHits();
            printOK(statement.eval(mainContext).toShortString(1024 * 1024));
        }
        catch (ParserException e)
        {
            printError("Syntax: " + e.getMessage());
        }
        catch (DebuggerException e)
        {
            printError("Debug: " + e.getMessage());
        }
        catch (RuntimeException e)
        {
            printError("Runtime: " + e);
        }
        catch (VDMErrorsException e)
        {
            printError(e.toString());
        }
        catch (Exception e)
        {
            printError("Error: " + e.getMessage());
        }

        return true;
    }

    @Override
    protected void println(String m) {
    }

    protected void printOK(Object result) {
        Object[] array = new Object[2];
        array[0] = true;
        array[1] = result;
        super.println(JSON.encode(array));
    }
    protected void printError(String message) {
        Object[] array = new Object[2];
        array[0] = false;
        array[1] = message;
        super.println(JSON.encode(array));
    }

}
