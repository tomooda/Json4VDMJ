# Json4VDMJ

Json4VDMJ is a wrapper on top of [VDMJ](https://github.com/nickbattle/vdmj), a VDM interpreter implemented in Java.

As VDMJ is distributed under the GPLv3, Json4VDMJ is also under the GPLv3.

How to use
---

The easiest way is to use [ViennaTalk](http://smalltalkhub.com/#!/~tomooda/ViennaTalk)

If you want to use it alone, place vdmj-&lt;version&gt;.jar and jsonic-&lt;version&gt;.jar in the same directory with json4vdmj.jar.

Commands
---

Jon4VDMJ provides a REPL interface. The below is a list of commands.

* load &lt;filename[,&lt;filename&gt;...] ... load a VDM-SL source file(s).
* init ... initialize the interpreter.
* modules ... returns a list of modules in the current specification.
* state ... returns a map of variables and values in the current animation state.
* default &lt;module name&gt; ... set the module to evaluate expressios and statements in.
* print &lt;expression&gt; ... returns a value of the given expression.
* statement &lt;statement&gt; ... execute the given statement.
* classic ... set the language to the classic VDM-SL.
* vdm10 ... set the language to the VDM10.
* +rtc ... enable runtime checking, e.g. dynamic type checking and assertions.
* -rtc ... disable runtime checking, e.g. dynamic type checking and assertions.

Output
---

Json4VDMJ responds in the following format:

[&lt;status&gt;, &lt;response body&gt;]

where

* &lt;status&gt; is either "OK" or "ERROR".
* &lt;response body&gt; is a list of strings for the modules command, a JSON object with keys from variable names and values from their values, otherwise a string.
