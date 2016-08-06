package il.org.spartan.refactoring.wring;

import il.org.spartan.refactoring.preferences.*;
import il.org.spartan.refactoring.preferences.PluginPreferencesResources.*;
import il.org.spartan.refactoring.utils.*;
import il.org.spartan.refactoring.wring.Wring.ReplaceCurrentNode;
import java.util.*;
import org.eclipse.jdt.core.dom.*;

/** Used to replace
 *
 * <pre>
 * s.equals("s")
 * </pre>
 *
 * </code> with safer
 *
 * <pre>
 * "s".equals(s)
 * </pre>
 *
 * @author Ori Roth
 * @since 2016/05/08 */
public class StringEqualsConstant extends ReplaceCurrentNode<MethodInvocation> {
  final static String[] _mns = { "equals", "equalsIgnoreCase" };
  final static List<String> mns = Arrays.asList(_mns);
  @SuppressWarnings("unchecked") @Override ASTNode replacement(final MethodInvocation i) {
    if (!mns.contains(i.getName().toString()) || i.arguments().size() != 1 || i.getExpression() == null || i.getExpression() instanceof StringLiteral
        || !(i.arguments().get(0) instanceof StringLiteral))
      return null;
    final MethodInvocation $ = i.getAST().newMethodInvocation();
    $.setExpression(Funcs.duplicate((Expression) i.arguments().get(0)));
    $.setName(Funcs.duplicate(i.getName()));
    $.arguments().add(Funcs.duplicate(i.getExpression()));
    return $;
  }
  @Override String description(final MethodInvocation i) {
    return "use " + i.arguments().get(0) + "." + i.getName() + "(" + i.getExpression() + ") instead of " + i;
  }
  @Override WringGroup wringGroup() {
    return WringGroup.REORDER_EXPRESSIONS;
  }
}