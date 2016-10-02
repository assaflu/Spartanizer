package il.org.spartan.spartanizer.leonidas;

import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.jface.text.*;
import org.eclipse.text.edits.*;

import il.org.spartan.*;
import il.org.spartan.spartanizer.ast.navigate.*;
import il.org.spartan.spartanizer.dispatch.*;

/** @author Ori Marcovitch
 * @since 2016 */
public class TestFactory {
  public static String testcase(final String raw) {
    return linify(shortenIdentifiers(eliminateSpaces(raw)));
  }

  /** Renders the Strings a,b,c, ..., z, X1, X2, ... */
  static String renderIdentifier(final String old) {
    if (old.length() == 0)
      return "a";
    if (old.equals("z"))
      return "X1";
    if (old.length() == 1)
      return String.valueOf((char) (old.charAt(0) + 1));
    return "X" + String.valueOf(old.charAt(1) + 1);
  }

  /** Actually, implementing this might be trivial, I think applying toString()
   * on an AST does this automatically.
   * @param s
   * @return */
  private static String eliminateSpaces(final String s) {
    return s;
  }

  /** Separate the string to lines, like: trimmingOf("// From use case of
   * issue#1593\n" + // "public void f(){\n" + // " if(!g){\n" + // " foo();\n"
   * + // " bar();\n" + // " }\n" + // "}"//
   * @param s string to linify
   * @return */
  private static String linify(final String s) {
    return s;
  }

  private static String shortenIdentifiers(final String s) {
    final Map<String, String> renaming = new HashMap<>();
    final Wrapper<String> id = new Wrapper<>();
    id.set("");
    final Document document = new Document(ASTutils.wrapCode(s));
    final ASTParser parser = ASTParser.newParser(AST.JLS8);
    parser.setSource(document.get().toCharArray());
    final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
    final AST ast = cu.getAST();
    final ASTNode n = ASTutils.extractASTNode(s, cu);
    final ASTRewrite r = ASTRewrite.create(ast);
    n.accept(new ASTVisitor() {
      @Override public boolean preVisit2(final ASTNode ¢) {
        if (iz.simpleName(¢)) {
          final String name = ((SimpleName) ¢).getFullyQualifiedName();
          if (!renaming.containsKey(name)) {
            id.set(renderIdentifier(id.get()));
            renaming.put(name, id.get());
          }
          Tippers.rename((SimpleName) ¢, ast.newSimpleName(renaming.get(name)), n, r, null);
        }
        return true;
      }
    });
    try {
      r.rewriteAST(document, null).apply(document);
    } catch (MalformedTreeException | IllegalArgumentException | BadLocationException e) {
      e.printStackTrace();
    }
    return ASTutils.extractCode(s, document);
  }
}
