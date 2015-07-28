/*
 * Copyright (C) 2008-2009 Federal University of Pernambuco and 
 * University of Central Florida
 *
 * This file is part of AJML
 *
 * AJML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * AJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AJML; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * $Id: PostconditionMethodAdvice2.java,v 1.0 2009/01/25 7:32:21 henriquerebelo Exp $ 
 */

package org.jmlspecs.ajmlrac;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jmlspecs.checker.JmlMethodDeclaration;
import org.jmlspecs.checker.JmlTypeDeclaration;
import org.jmlspecs.util.AspectUtil;
import org.multijava.mjc.CType;
import org.multijava.mjc.JMethodDeclarationType;

import com.thoughtworks.qdox.model.JavaMethod;


/**
 * A class for generating a postcondition check method as an AspecJ advice.
 * The postcondition checking code is automatic wrapped with code that checks
 * inherited postconditions if any, and throws an appropriate exception to signal
 * a violation if the postcondition is violated at runtime.
 * <p/>
 * <p>
 * The class implements a variant of the <em>Template Pattern</em>
 * [GoF95], prescribed in the class {@link AssertionMethod}.
 * </p>
 *
 * @author Henrique Rebelo
 * @version $Revision: 1.0 $
 * @see AssertionMethod
 */

public class PostconditionMethodAdvice2 extends PreOrPostconditionMethod {

    // ----------------------------------------------------------------------
    // CONSTRUCTORS
    // ----------------------------------------------------------------------

    /**
     * Construct a new <tt>PostconditionMethodAdvice</tt> object.
     *
     * @param mdecl method for which the postcondition method is generated
     */
    public PostconditionMethodAdvice2(JmlTypeDeclaration typeDecl,
                                      JmlMethodDeclaration mdecl,
                                      String restoreMethod) {
        super(typeDecl, mdecl, restoreMethod);
        this.prefix = MN_CHECK_POST;
        this.methodName = prefix + methodName(mdecl) + "$" + typeDecl.ident();
        this.exceptionToThrow = "JMLExitNormalPostconditionError";
        this.returnType = mdecl.returnType();

        // javadoc to be added to the generated method
        boolean isMethodCrosscutSpecChecking = AspectUtil.getInstance().isCrosscutSpecChecking(this.methodDecl);
        if (isMethodCrosscutSpecChecking) {
            this.javadoc = "/** Generated by AspectJML to check the normal " +
                    "postcondition of\n" +
                    " * members intercepted by" + mdecl.ident() + " pointcut. */";
        } else {
            this.javadoc = "/** Generated by AspectJML to check the normal " +
                    "postcondition of\n" +
                    " * method " + mdecl.ident() + ". */";
        }
    }

    // ----------------------------------------------------------------------
    // TRANSLATION
    // ----------------------------------------------------------------------
    public JMethodDeclarationType generate(RacNode stmt) {
        throw new UnsupportedOperationException();
    }

    public JMethodDeclarationType generate(RacNode stmt, String prePred,
                                           String contextForPre, String tokenReferenceForPre,
                                           String nPostPred, List xPostCode, HashMap oldExprs, HashMap oldExprsDecl,
                                           List preExprs, List preExprsDecl, HashMap oldVarsDecl, String instrumentationType,
                                           long visibility, List<CType> exceptionsInSignalsClauses) {
        if (instrumentationType.equals("clientAwareChecking")) {
            if (visibility == ACC_PUBLIC) {
                this.preconditionThrow = "JMLEntryPublicPreconditionError";
                this.exceptionToThrow = "JMLExitPublicNormalPostconditionError";
                this.exceptionToXThrow = "JMLExitPublicExceptionalPostconditionError";
            } else if (visibility == ACC_PROTECTED) {
                this.preconditionThrow = "JMLEntryProtectedPreconditionError";
                this.exceptionToThrow = "JMLExitProtectedNormalPostconditionError";
                this.exceptionToXThrow = "JMLExitProtectedExceptionalPostconditionError";
            } else if (visibility == 0L) { //default
                this.preconditionThrow = "JMLEntryDefaultPreconditionError";
                this.exceptionToThrow = "JMLExitDefaultNormalPostconditionError";
                this.exceptionToXThrow = "JMLExitDefaultExceptionalPostconditionError";
            } else if (visibility == ACC_PRIVATE) {
                this.preconditionThrow = "JMLEntryPrivatePreconditionError";
                this.exceptionToThrow = "JMLExitPrivateNormalPostconditionError";
                this.exceptionToXThrow = "JMLExitPrivateExceptionalPostconditionError";
            }
        } else {
            this.exceptionToXThrow = "JMLExitExceptionalPostconditionError";
        }
        this.exceptionsInSignalsClauses = exceptionsInSignalsClauses;
        this.hasOldVariables = AspectUtil.getInstance().hasElementsStoredOldExpressions(oldVarsDecl);
        this.hasOldExpressions = AspectUtil.getInstance().hasElementsStoredOldExpressions(oldExprs);
        this.hasPreExpressions = preExprs.size() > 0;
        StringBuffer code = new StringBuffer("");
        if (hasPreExpressions || hasOldVariables || hasOldExpressions) {
            code = this.generatePreconditionNormalAndXPostconditionWithAroundAdvice(
                    prePred, contextForPre, tokenReferenceForPre,
                    nPostPred, xPostCode,
                    oldExprs, oldExprsDecl,
                    preExprs, preExprsDecl, oldVarsDecl,
                    instrumentationType, visibility);
        }
        return RacParser.parseMethod(code.toString(), stmt);
    }

    private void setJavadocForNPostAndXPostMethodAdvice(long visibility) {
        boolean isMethodCrosscutSpecChecking = AspectUtil.getInstance().isCrosscutSpecChecking(this.methodDecl);
        if (isMethodCrosscutSpecChecking) {
            this.javadoc = "/** Generated by AspectJML to check the precondition, normal " +
                    "and\n" +
                    " * exceptional postcondition of members intercepted by " + this.methodDecl.ident() + " pointcut. */";
        } else {
            this.javadoc = "/** Generated by AspectJML to check the precondition, normal " +
                    "and\n" +
                    " * exceptional postcondition of method " + this.methodDecl.ident() + ". */";
        }

        if (visibility == ACC_PUBLIC) {
            this.javadoc = this.javadoc.replace("precondition", "public precondition").replace("postcondition", "public postcondition");
        } else if (visibility == ACC_PROTECTED) {
            this.javadoc = this.javadoc.replace("precondition", "protected precondition").replace("postcondition", "protected postcondition");
        } else if (visibility == 0L) {// default
            this.javadoc = this.javadoc.replace("precondition", "default precondition").replace("postcondition", "default postcondition");
        } else if (visibility == ACC_PRIVATE) {
            this.javadoc = this.javadoc.replace("precondition", "private precondition").replace("postcondition", "private postcondition");
        }
    }

    /**
     * @param prePred              precondition syntactic string.
     * @param contextForPre
     * @param tokenReferenceForPre
     * @param nPostPred            normal post-condition syntactic string.
     * @param xPostCode            exceptional post-condition syntactic string.
     * @param oldExprs
     * @param oldExprsDecl
     * @param preExprs
     * @param preExprsDecl
     * @param oldVarsDecl
     * @param instrumentationType
     * @param visibility
     * @return advice code
     */
    private StringBuffer generatePreconditionNormalAndXPostconditionWithAroundAdvice(
            String prePred, String contextForPre, String tokenReferenceForPre,
            String nPostPred, List xPostCode,
            HashMap oldExprs, HashMap oldExprsDecl,
            List preExprs, List preExprsDecl, HashMap oldVarsDecl,
            String instrumentationType, long visibility) {
        StringBuffer code = null;
        String contextValuesPre = contextForPre;
        String mn = "";
        JavaMethod jm = AspectUtil.getInstance().getCorrespondingJavaMethodThroughJMLMethod(this.methodDecl.getMethod().owner().getJavaName(), this.methodDecl);
        String methodReturnType = "";
        boolean isFlexibleXCS = false;
        if (jm != null) {
            isFlexibleXCS = AspectUtil.getInstance().isXCSFlexible(jm);
        }
        if ((jm != null) && (!(this.methodDecl.isConstructor()))) {
            if (jm.getReturnType().toString().equals(this.methodDecl.returnType().toString())) {
                methodReturnType = this.methodDecl.returnType().toString();
            } else {
                methodReturnType = jm.getReturnType().toString();
            }
        } else {
            methodReturnType = this.methodDecl.returnType().toString();
        }

        boolean isMethodCrosscutSpecChecking = AspectUtil.getInstance().isCrosscutSpecChecking(this.methodDecl);
        if (this.methodDecl.isStatic() || this.methodDecl.isConstructor()) {
            mn = this.methodDecl.getMethod().getJavaName() + AspectUtil.generateMethodParameters(this.parameters, false).toString();
        } else {
            mn = "." + this.methodDecl.getMethod().getIdent() + AspectUtil.generateMethodParameters(this.parameters, false).toString();
        }// mn = ".execute(int, int)"

        this.setJavadocForNPostAndXPostMethodAdvice(visibility);
        code = this.buildAdviceHeader("NPAndXPAssertionMethodsWithAroundAdvice", instrumentationType, visibility, isMethodCrosscutSpecChecking);
        code.append(" {\n");// start body
        if (isMethodCrosscutSpecChecking) {
            code.append("     String runtimeObjectOrStaticType = \"\";\n");
            code.append("     String methSig = \"\";\n");
            code.append("     if(thisJoinPoint.getKind().equals(thisJoinPoint.CONSTRUCTOR_CALL)){\n");
            code.append("       runtimeObjectOrStaticType = thisJoinPoint.getSignature().getDeclaringTypeName();\n");
            code.append("       methSig = thisJoinPoint.getSignature().toLongString().substring(thisJoinPoint.getSignature().toLongString().indexOf(runtimeObjectOrStaticType));\n");
            code.append("       methSig = methSig.replace(runtimeObjectOrStaticType, runtimeObjectOrStaticType+\".<init>\");\n");
            code.append("     }\n");
            code.append("     else{\n");
            code.append("       if(java.lang.reflect.Modifier.isStatic(thisJoinPoint.getSignature().getModifiers())){\n");
            code.append("          runtimeObjectOrStaticType = thisJoinPoint.getSignature().getDeclaringTypeName();\n");
            code.append("       }\n");
            code.append("       else{\n");
            code.append("         if(java.lang.reflect.Modifier.isStatic(thisJoinPoint.getSignature().getModifiers())){\n");
            code.append("          runtimeObjectOrStaticType = thisJoinPoint.getSignature().getDeclaringTypeName();\n");
            code.append("         }\n");
            code.append("         else{\n");
            code.append("           if(thisJoinPoint.getThis() != null){\n");
            code.append("             runtimeObjectOrStaticType = thisJoinPoint.getThis().getClass().getName() + \"@\" + Integer.toHexString(System.identityHashCode(thisJoinPoint.getThis()));\n");
            code.append("           }\n");
            code.append("           else {\n");
            code.append("             runtimeObjectOrStaticType = thisJoinPoint.getTarget().getClass().getName() + \"@\" + Integer.toHexString(System.identityHashCode(thisJoinPoint.getTarget()));\n");
            code.append("           }\n");
            code.append("         }\n");
            code.append("       }\n");
            code.append("       methSig = thisJoinPoint.getSignature().toLongString().substring(thisJoinPoint.getSignature().toLongString().indexOf(thisJoinPoint.getSignature().getName()));\n");
            code.append("     }\n");
        }
        if (isMethodCrosscutSpecChecking) {
            code.append("    ").append("java.lang.Object").append(" ").append("rac$result");
            code.append(" = ").append("null").append(";\n");
        } else {
            if (!(methodReturnType.equals("void"))) {
                code.append("    ").append(AspectUtil.processMethSig(methodReturnType)).append(" ").append("rac$result");
                code.append(" = ").append(TransUtils.defaultValue(methodReturnType)).append(";\n");
            } else if (methodDecl.isConstructor() && (instrumentationType.equals("callSite") || instrumentationType.equals("clientAwareChecking"))) {
                code.append("    ").append(AspectUtil.processMethSig(this.methodDecl.getMethod().owner().getJavaName())).append(" ").append("rac$result");
                code.append(" = ").append(TransUtils.defaultValue(methodReturnType)).append(";\n");
            }
        }
        HashMap preconditions = getPreconditions(visibility);
        if (this.hasPreExpressions || this.hasOldVariables || this.hasOldExpressions) {
            if (this.hasOldVariables) {
                for (Iterator iterator = preconditions.keySet().iterator(); iterator.hasNext(); ) {
                    int index = (int) iterator.next();
                    List oldVarsDeclList = (List) oldVarsDecl.get(index);
                    if (oldVarsDeclList != null) {
                        for (Iterator iterator2 = oldVarsDeclList.iterator(); iterator2
                                .hasNext(); ) {
                            String currentOldVarDecl = (String) iterator2.next();
                            code.append("    final " + currentOldVarDecl.substring(0, currentOldVarDecl.indexOf('/')) + ";");
                            code.append("\n");
                        }
                    }
                }
            }
            if (this.hasPreExpressions) {
                for (Iterator iterator = preExprsDecl.iterator(); iterator.hasNext(); ) {
                    String currentPreExprsDecl = (String) iterator.next();
                    code.append("    boolean " + currentPreExprsDecl + ";\n");

                }
            }
            if (this.hasOldExpressions) {
                for (Iterator iterator = preconditions.keySet().iterator(); iterator.hasNext(); ) {
                    int index = (int) iterator.next();
                    List oldExprsDeclList = (List) oldExprsDecl.get(index);
                    if (oldExprsDeclList != null) {
                        for (Iterator iterator2 = oldExprsDeclList.iterator(); iterator2
                                .hasNext(); ) {
                            String currentOldExprsDecl = (String) iterator2.next();
                            code.append("    final " + currentOldExprsDecl);
                            code.append("\n");
                        }
                    }
                }
            }
            // saving pre-expressions and precondition related old vars
            boolean canGenerate = false;
            if (this.hasOldVariables) {
                outerLoop:
                for (Iterator iterator = preconditions.keySet().iterator(); iterator.hasNext(); ) {
                    List oldVarsDeclList = (List) oldVarsDecl.get(iterator.next());
                    if (oldVarsDeclList != null) {
                        for (Iterator iterator2 = oldVarsDeclList.iterator(); iterator2
                                .hasNext(); ) {
                            String currentOldVar = (String) iterator2.next();
                            String[] currentOldVarParts = currentOldVar.substring(0, currentOldVar.indexOf('/')).replace(";", "").split(" ");
                            String currentOldVarIdent = currentOldVarParts[1];
                            if (AspectUtil.getInstance().isOldVarReferencedWithinPrecondition(preconditions, currentOldVarIdent)) {
                                canGenerate = true;
                                break outerLoop;
                            }
                            if (AspectUtil.getInstance().isOldVarReferencedWithinPreExpr(preExprs, currentOldVarIdent)) {
                                canGenerate = true;
                                break outerLoop;
                            }
                        }
                    }
                }
            }
            if (this.hasPreExpressions) {
                Iterator iterator = preExprs.iterator();
                if (iterator.hasNext()) {
                    canGenerate = true;
                }
            }
            if (canGenerate) {
                code.append("cute.Cute.Assert(").append(AspectUtil.changeThisOrSuperRefToAdviceRef(prePred, typeDecl)).append(");\n");
                code.append("      // saving pre-expressions and precondition related old vars\n");
                if (this.hasOldVariables) {
                    for (Iterator iterator = preconditions.keySet().iterator(); iterator.hasNext(); ) {
                        int index = (int) iterator.next();
                        List oldVarsDeclList = (List) oldVarsDecl.get(index);
                        if (oldVarsDeclList != null) {
                            for (Iterator iterator2 = oldVarsDeclList.iterator(); iterator2
                                    .hasNext(); ) {
                                String currentOldVar = (String) iterator2.next();
                                String[] currentOldVarParts = currentOldVar.substring(0, currentOldVar.indexOf('/')).replace(";", "").split(" ");
                                String currentOldVarIdent = currentOldVarParts[1];
                                if (AspectUtil.getInstance().isOldVarReferencedWithinPrecondition(preconditions, currentOldVarIdent)) {
                                    code.append("\t\t" + AspectUtil.changeThisOrSuperRefToAdviceRef((currentOldVar.substring((currentOldVar.indexOf('/') + 1))), typeDecl) + ";\n");
                                }
                                if (AspectUtil.getInstance().isOldVarReferencedWithinPreExpr(preExprs, currentOldVarIdent)) {
                                    code.append("\t\t" + AspectUtil.changeThisOrSuperRefToAdviceRef((currentOldVar.substring((currentOldVar.indexOf('/') + 1))), typeDecl) + ";\n");
                                }
                            }
                        }
                    }
                }
                if (this.hasPreExpressions) {
                    for (Iterator iterator = preExprs.iterator(); iterator.hasNext(); ) {
                        String currentPreExpr = (String) iterator.next();
                        // adding JML quantifierInnerClasses if any
                        code.append(this.getQuantifierInnerClasses(currentPreExpr));
                        code.append("\t\t");
                        code.append(currentPreExpr);
                        code.append("\n");
                    }
                }
            }
        }
        // saving old expressions or old variables not mentioned within preconditions
        for (Iterator iterator = preconditions.keySet().iterator(); iterator.hasNext(); ) {
            int index = (int) iterator.next();
            List oldVarsDeclList = (List) oldVarsDecl.get(index);
            List oldExprsList = (List) oldExprs.get(index);
            boolean canGenerate = false;
            if ((oldVarsDeclList != null)) {
                for (Iterator iterator2 = oldVarsDeclList.iterator(); iterator2
                        .hasNext(); ) {
                    String currentOldVar = (String) iterator2.next();
                    String[] currentOldVarParts = currentOldVar.substring(0, currentOldVar.indexOf('/')).replace(";", "").split(" ");
                    String currentOldVarIdent = currentOldVarParts[1];
                    if (!AspectUtil.getInstance().isOldVarReferencedWithinPrecondition(preconditions, currentOldVarIdent) &&
                            !AspectUtil.getInstance().isOldVarReferencedWithinPreExpr(preExprs, currentOldVarIdent)) {
                        canGenerate = true;
                        break;
                    }
                }
            }
            if ((oldExprsList != null)) {
                for (Iterator iterator2 = oldExprsList.iterator(); iterator2
                        .hasNext(); ) {
                    canGenerate = true;
                    break;
                }
            }
            if (canGenerate) {
                code.append("    // saving old expressions and old vars related to each spec case\n");
                if ((oldVarsDeclList != null)) {
                    for (Iterator iterator2 = oldVarsDeclList.iterator(); iterator2
                            .hasNext(); ) {
                        String currentOldVar = (String) iterator2.next();
                        String[] currentOldVarParts = currentOldVar.substring(0, currentOldVar.indexOf('/')).replace(";", "").split(" ");
                        String currentOldVarIdent = currentOldVarParts[1];
                        if (!AspectUtil.getInstance().isOldVarReferencedWithinPrecondition(preconditions, currentOldVarIdent) &&
                                !AspectUtil.getInstance().isOldVarReferencedWithinPreExpr(preExprs, currentOldVarIdent)) {
                            code.append("    " + AspectUtil.changeThisOrSuperRefToAdviceRef((currentOldVar.substring((currentOldVar.indexOf('/') + 1))), typeDecl) + ";");
                            code.append("\n");
                        }
                    }
                }
                if ((oldExprsList != null)) {
                    for (Iterator iterator2 = oldExprsList.iterator(); iterator2
                            .hasNext(); ) {
                        String currentOldExpr = (String) iterator2.next();
                        code.append("    " + AspectUtil.changeThisOrSuperRefToAdviceRef(currentOldExpr, this.typeDecl));
                        code.append("\n");
                    }
                }
            }
        }
        String qcode = this.getQuantifierInnerClasses(nPostPred.replace("object$rac", "rac$result"));
        if (isMethodCrosscutSpecChecking) {
            if (qcode.contains("rac$result")) {
                code.append("    final ").append(methodReturnType).append(" ").append("rac$result$qcode;\n");
                qcode = qcode.replace("rac$result", "rac$result$qcode");
            }
        } else {
            if (!(methodReturnType.equals("void"))) {
                if (qcode.contains("rac$result")) {
                    code.append("    final ").append(methodReturnType).append(" ").append("rac$result$qcode;\n");
                    qcode = qcode.replace("rac$result", "rac$result$qcode");
                }
            } else if (methodDecl.isConstructor() && (instrumentationType.equals("callSite") || instrumentationType.equals("clientAwareChecking"))) {
                if (qcode.contains("rac$result")) {
                    code.append("    final ").append(this.methodDecl.getMethod().owner().getJavaName()).append(" ").append("rac$result$qcode;\n");
                    qcode = qcode.replace("rac$result", "rac$result$qcode");
                } else if (qcode.contains("object$rac")) {
                    code.append("    final ").append(this.methodDecl.getMethod().owner().getJavaName()).append(" ").append("rac$result$qcode;\n");
                    qcode = qcode.replace("object$rac", "rac$result$qcode");
                }
            }
        }
        code.append("    try {\n");// surround method execution with try-catch.
        {
            if (isMethodCrosscutSpecChecking) {// method execution.
                code.append("      ").append("rac$result").append(" = ").append(this.buildCallProceed(parameters, instrumentationType, isMethodCrosscutSpecChecking, isFlexibleXCS)).append(";").append("//executing the method\n");
                if (qcode.contains("rac$result")) {
                    code.append("      ").append("rac$result$qcode").append(" = ").append("rac$result;\n");
                }
            } else {
                if ((!(methodReturnType.equals("void"))) || (methodDecl.isConstructor() && (instrumentationType.equals("callSite") || instrumentationType.equals("clientAwareChecking")))) {
                    code.append("      ").append("rac$result").append(" = ").append(this.buildCallProceed(parameters, instrumentationType, isMethodCrosscutSpecChecking, isFlexibleXCS)).append(";").append("//executing the method\n");
                    if (qcode.contains("rac$result")) {
                        code.append("      ").append("rac$result$qcode").append(" = ").append("rac$result;\n");
                    }
                } else {
                    code.append("      ").append(this.buildCallProceed(parameters, instrumentationType, isMethodCrosscutSpecChecking, isFlexibleXCS)).append(";").append("//executing the method\n");
                }
            }
            code.append(qcode);
            // Normal post-condition.
            HashMap nPostconditions = getNormalPostconditions(visibility);
            for (Iterator iterator = preconditions.keySet().iterator(); iterator.hasNext(); ) {
                String normalPostcondition = (String) nPostconditions.get(iterator.next());
                if (AspectUtil.hasAssertion(normalPostcondition)) {
                    if (methodDecl.isConstructor() && (visibility != -1)) {
                        normalPostcondition = normalPostcondition.replace("object$rac", "rac$result");
                    }
                    //TODO: Use cute.Cute.Assume, if method is not under test.
                    code.append("      cute.Cute.Assert(").append(normalPostcondition).append(");\n");
                }
            }
        }
        // Exceptional post-conditions.
        code.append("    } catch (Throwable rac$e) {\n");
        {
            for (Iterator iterator = xPostCode.iterator(); iterator.hasNext(); ) {
                code.append(iterator.next());
            }
            if (xPostCode.size() > 0) {
                code.append("\n");
            }
        }
        code.append("\t\t}\n");
        if (isMethodCrosscutSpecChecking || !(methodReturnType.equals("void")) || (methodDecl.isConstructor() && (instrumentationType.equals("callSite") || instrumentationType.equals("clientAwareChecking")))) {
            code.append("    ").append("return rac$result;").append("\n");
        }
        code.append("\t}").append("\n");
        if (methodDecl.isConstructor() && (instrumentationType.equals("callSite") || instrumentationType.equals("clientAwareChecking"))) {
            String codeTmp = code.toString();
            codeTmp = codeTmp.replace("object$rac", "rac$result");
            code = new StringBuffer(codeTmp);
        }
        return code;
    }

    // ----------------------------------------------------------------------
    // DATA MEMBERS
    // ----------------------------------------------------------------------
    /**
     * Return type of the method declaration
     */
    protected CType returnType;
    protected boolean hasPreExpressions;
    protected boolean hasOldVariables;
    protected boolean hasOldExpressions;
    private static String preconditionThrow = "JMLInternalPreconditionError";
    private String exceptionToXThrow;
    private List<CType> exceptionsInSignalsClauses;
}
