/*
	Copyright (c) 2009 Olivier Chafik, All Rights Reserved
	
	This file is part of JNAerator (http://jnaerator.googlecode.com/).
	
	JNAerator is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	JNAerator is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with JNAerator.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.ochafik.lang.jnaerator.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.ochafik.util.string.StringUtils;

public class Function extends Declaration {
	//private Struct owner;

	final List<Arg> args = new ArrayList<Arg>();
	Element body;
	Type type;

	public enum Type {
		CFunction, ObjCMethod, CppMethod, JavaMethod
	}
	

	@Override
	public Element getNextChild(Element child) {
		Element e = super.getNextChild(child);
		if (e != null)
			return e;
		return getNextSibling(args, child);
	}
	
	@Override
	public Function clone() {
		return (Function) super.clone();
	}
	
	@Override
	public Element getPreviousChild(Element child) {
		Element e = super.getPreviousChild(child);
		if (e != null)
			return e;
		return getPreviousSibling(args, child);
	}

	@Override
	public boolean replaceChild(Element child, Element by) {
		if (child == getBody()) {
			setBody(by);
			return true;
		}
		
		if (replaceChild(args, Arg.class, this, child, by))
			return true;
		
		return super.replaceChild(child, by);
	}
	
	
	
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public Arg addArg(Arg a) {
		if (a != null) {
			args.add(a);
			a.setParentElement(this);
		}
		return a;
	}
	public List<Arg> getArgs() {
		return unmodifiableList(args);
	}
	public void setArgs(List<Arg> args) {
		changeValue(this, this.args, args);
	}
	
	public void setBody(Element body) {
		this.body = changeValue(this, this.body, body);
	}

	//public static class CFunction extends Function {
	public Function() {}

	public Function(String name, TypeRef returnType) {
		setName(name);
		setValueType(returnType);
	}


	@Override
	public String toString(CharSequence indent) {
		String s = "";
		TypeRef valueType = getValueType();
		String name = getName();
		Set<Modifier> modifiers = getModifiers();
		
		if (type == null)
			return "<no function type>";
		
		String pre = formatComments(indent, false);
		if (pre.length() > 0)
			pre += "\n" + indent;
		String //pre = commentBefore == null ? "" : commentBefore + "\n" + indent, 
				post = commentAfter == null ? "" : commentAfter;//" /*" + commentAfter + " */";
		
		if (!getAnnotations().isEmpty())
			pre += StringUtils.implode(getAnnotations(), "\n" + indent) + "\n" + indent;
		
		switch (type) {
		case CFunction:
		case CppMethod:
		case JavaMethod:
			String preMods = StringUtils.implode(modifiers, " ") + (modifiers.isEmpty() ? "" : " ");
			s = preMods + 
				(valueType == null ? "" : valueType + " ") +
				name + "(" +
				StringUtils.implode(args, ", ") +
				")";

			return pre + s + (body == null ? ";" : " {\n" + body +"\n}") + post;
		case ObjCMethod:
			s = modifiers.contains(Modifier.Static) ? "+" : "-";
			StringBuilder argsStr = new StringBuilder();
			for (Arg arg : args) {
				if (arg.isVarArg()) {
					if (argsStr.length() > 0)
						argsStr.append(", ");
					argsStr.append("...");
				} else {
					if (argsStr.length() > 0)
					{
						argsStr.append(' ');
						argsStr.append(arg.getSelector());
					}
					argsStr.append(":(");
					argsStr.append(arg.getValueType());
					argsStr.append(')');
					argsStr.append(arg.getName());
				}
			}
			return pre + s + " " + "(" + getValueType() + ")" + name + argsStr + ";" + post;
		default:
			throw new NoSuchElementException(type.toString());
		}
	}

	public void accept(Visitor visitor) {
		visitor.visitFunction(this);
	}
	public Element getBody() {
		return body;
	}

	public String computeSignature() {
		StringBuilder b = new StringBuilder();
		b.append(getName());
		boolean first = true;
		for (Arg arg : getArgs()) {
			if (first) {
				b.append('(');
				first = false;
			} else 
				b.append(", ");
			b.append(arg.getValueType());
		}
		b.append(')');
		return b.toString();
	}


}
