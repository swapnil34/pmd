/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.vf.rule.security;

import java.util.List;

/*
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.vf.ast.ASTArguments;
import net.sourceforge.pmd.lang.vf.ast.ASTAttribute;
import net.sourceforge.pmd.lang.vf.ast.ASTContent;
import net.sourceforge.pmd.lang.vf.ast.ASTDotExpression;
import net.sourceforge.pmd.lang.vf.ast.ASTElExpression;
import net.sourceforge.pmd.lang.vf.ast.ASTElement;
import net.sourceforge.pmd.lang.vf.ast.ASTExpression;
import net.sourceforge.pmd.lang.vf.ast.ASTHtmlScript;
import net.sourceforge.pmd.lang.vf.ast.ASTIdentifier;
import net.sourceforge.pmd.lang.vf.ast.ASTLiteral;
import net.sourceforge.pmd.lang.vf.ast.ASTNegationExpression;
import net.sourceforge.pmd.lang.vf.ast.ASTText;
import net.sourceforge.pmd.lang.vf.ast.AbstractVFNode;
import net.sourceforge.pmd.lang.vf.rule.AbstractVfRule;
*/

import net.sourceforge.pmd.lang.vf.ast.ASTAttribute;
import net.sourceforge.pmd.lang.vf.ast.ASTElExpression;
import net.sourceforge.pmd.lang.vf.ast.ASTElement;
import net.sourceforge.pmd.lang.vf.ast.ASTIdentifier;
import net.sourceforge.pmd.lang.vf.rule.AbstractVfRule;

/**
 * @author swapnil.shinde
 * detection pattern
 * <lightning:container aura:id="ReactApp"/>
 *                            
 *
 */
public class VfLCCdetectionRule extends AbstractVfRule {

	private static final String LIGHTNING_CONTAINER = "lightning:container";
	boolean isLCC = false;

    @Override
    public Object visit(ASTElement node, Object data) {
    	System.out.println(node.getName());
        if (LIGHTNING_CONTAINER.equalsIgnoreCase(node.getName())) {
            
        	isLCC = true;
        	addViolation(data, node);
            
        }
    return super.visit(node, data);
    }
}

