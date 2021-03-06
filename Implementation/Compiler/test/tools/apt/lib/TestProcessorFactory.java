/*
 * Copyright 2004-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */


import java.util.*;
import com.sun.mirror.apt.*;
import com.sun.mirror.declaration.*;
import com.sun.mirror.util.*;


/**
 * A factory for generating the TestProcessor annotation processor, which
 * processes the @Test annotation.
 *
 * @author Scott Seligman
 */
public class TestProcessorFactory implements AnnotationProcessorFactory {

    public Collection<String> supportedOptions() {
	return new ArrayList<String>();
    }

    public Collection<String> supportedAnnotationTypes() {
	ArrayList<String> res = new ArrayList<String>();
	res.add("Test");
	res.add("Ignore");
	return res;
    }

    public AnnotationProcessor getProcessorFor(
					Set<AnnotationTypeDeclaration> as,
					AnnotationProcessorEnvironment env) {
	// The tester that's running.
	Tester tester = Tester.activeTester;

	try {
	    // Find the tester's class declaration.
	    ClassDeclaration testerDecl = null;
	    for (TypeDeclaration decl : env.getSpecifiedTypeDeclarations()) {
		if (decl.getQualifiedName().equals(
					       tester.getClass().getName())) {
		    testerDecl = (ClassDeclaration) decl;
		    break;
		}
	    }

	    // Give the tester access to its own declaration and to the env.
	    tester.thisClassDecl = testerDecl;
	    tester.env = env;

	    // Initializer the tester.
	    tester.init();

	    return new TestProcessor(env, tester);

	} catch (Exception e) {
	    throw new Error("Couldn't create test annotation processor", e);
	}
    }
}
