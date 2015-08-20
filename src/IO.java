/*******************************************************************************
 *
 *	Copyright (C) 2008, 2009 Fujitsu Services Ltd.
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

import org.overturetool.vdmj.runtime.Context;
import org.overturetool.vdmj.values.*;

import java.io.Serializable;
public class IO implements Serializable
{
    private static final long serialVersionUID = 1L;


    public static Value writeval(Value tval)
    {
        return new BooleanValue(false);
    }

    public static Value fwriteval(Value fval, Value tval, Value dval)
    {
        return new BooleanValue(false);
    }

    // Note that this method is not callable via the native interface, since it
    // need access to the Context to call any type invariants involved while
    // reading the data.

    public static Value freadval(Value fval, Context ctxt)
    {
        ValueList result = new ValueList();
        result.add(new BooleanValue(false));
        result.add(new NilValue());
        return new TupleValue(result);
    }

    public static Value fecho(Value fval, Value tval, Value dval)
    {
        return new BooleanValue(false);
    }

    public static Value ferror()
    {
        return new SeqValue("IO is not allowed.");
    }

    public static Value print(Value v)
    {
        return new VoidValue();
    }

    public static Value println(Value v)
    {
        return new VoidValue();
    }

    public static Value printf(Value fv, Value vs)
    {
        return new VoidValue();
    }
}
