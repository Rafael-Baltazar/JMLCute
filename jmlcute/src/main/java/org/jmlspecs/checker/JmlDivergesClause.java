/*
 * Copyright (C) 2000-2001 Iowa State University
 *
 * This file is part of mjc, the MultiJava Compiler, and the JML Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id: JmlDivergesClause.java,v 1.5 2005/09/12 19:02:00 cruby Exp $
 */

package org.jmlspecs.checker;

import org.multijava.mjc.CFlowControlContextType;
import org.multijava.mjc.MjcVisitor;
import org.multijava.util.compiler.TokenReference;

/**
 * A JML AST node class for the <tt>diverges</tt> clause.
 *
 * @author Yoonsik Cheon
 * @version $Revision: 1.5 $
 */

public class JmlDivergesClause extends JmlPredicateClause {

    //---------------------------------------------------------------------
    // CONSTRUCTORS 
    //---------------------------------------------------------------------
    public JmlDivergesClause( TokenReference where,
			      boolean isRedundantly, JmlPredicate predOrNot ) {
	super( where, isRedundantly, predOrNot );
    }
    
    //---------------------------------------------------------------------
    // ACCESSORS
    //---------------------------------------------------------------------

    //@ pure
    public int preferredOrder() {
	return PORDER_DIVERGES_CLAUSE;
    }

    //---------------------------------------------------------------------
    // DERIVED ATTRIBUTES
    //---------------------------------------------------------------------

    //---------------------------------------------------------------------
    // INTERFACE CHECKING
    //---------------------------------------------------------------------

    //---------------------------------------------------------------------
    // TYPECHECKING
    //---------------------------------------------------------------------


    /** Returns an appropriate context for checking this clause. */
    protected JmlExpressionContext createContext(
        CFlowControlContextType context) {
        return JmlExpressionContext.createPrecondition( context );
    }

    //---------------------------------------------------------------------
    // CODE GENERATION
    //---------------------------------------------------------------------

    /**
     * Accepts the specified visitor.
     * @param p	the visitor
     */
    public void accept( MjcVisitor p ) {
	if (p instanceof JmlVisitor)
	    ((JmlVisitor)p).visitJmlDivergesClause(this);
	else
	    throw new UnsupportedOperationException(JmlNode.MJCVISIT_MESSAGE);
    }

    //---------------------------------------------------------------------
    // PRIVILEGED DATA
    //---------------------------------------------------------------------
}
