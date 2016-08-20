package il.org.spartan.refactoring.wring;

import static il.org.spartan.refactoring.utils.Funcs.*;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.*;

import java.util.*;

import org.eclipse.jdt.core.dom.*;

import il.org.spartan.refactoring.utils.*;
import il.org.spartan.refactoring.wring.Wring.*;

/** Replace <code>0+X</code>, <code>X+0</code> and <code>X-0</code> by <code>X</code>
 * @author Alex Kopzon
 * @author Dan Greenstein
 * @since 2016 */
public final class InfixNeutralAdditionSubstruction extends ReplaceCurrentNode<InfixExpression> implements Kind.NoImpact {
  @Override String description(final InfixExpression e) {
    return "Remove all additions and substructions of 0 to and from " + e;
  }
  @Override ASTNode replacement(final InfixExpression e) {
    //return e.getOperator() == PLUS ? replacementPlus(extract.allOperands(e), e) : e.getOperator() == MINUS ? replacementMinus(extract.allOperands(e), e) : null;
    return e.getOperator() == MINUS ? replacementMinus(extract.allOperands(e), e) : e.getOperator() == PLUS ? replacementPlus(extract.allOperands(e), e) : null;
  }
  private static ASTNode replacementPlus(final List<Expression> es, final InfixExpression e) {
    System.out.println("" + e.getOperator());
    return isLiteralZero(es.get(0)) ? duplicate(es.get(1)) : isLiteralZero(es.get(1)) ? duplicate(es.get(0)) : null;
  }
  private static ASTNode replacementMinus(final List<Expression> es, final InfixExpression e) {
    System.out.println("" + e.getOperator());
    return isLiteralZero(es.get(1)) ? duplicate(es.get(0)) : null;
  }
  private static boolean isLiteralZero(final Expression ¢) {
    return isLiteralZero(asNumberLiteral(¢));
  }
  private static boolean isLiteralZero(final NumberLiteral ¢) {
    return ¢ != null && isLiteralZero(¢.getToken());
  }
  private static boolean isLiteralZero(final String ¢) {
    try {
      return Integer.parseInt(¢) == 0;
    } catch (@SuppressWarnings("unused") final NumberFormatException __) {
      return false;
    }
  }
}