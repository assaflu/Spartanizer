package org.spartan.refactoring.handlers;

import static org.spartan.refactoring.handlers.ApplySpartanizationHandler.applySafeSpartanizationsTo;
import static org.spartan.refactoring.spartanizations.DialogBoxes.announce;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.spartan.refactoring.spartanizations.Spartanization;
import org.spartan.refactoring.spartanizations.Spartanizations;

/**
 * A handler for {@link Spartanizations}. This handler executes all safe
 * Spartanizations on all java files in the current project.
 *
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code>
 * @since 2015/08/01
 */
public class CleanupHandler extends BaseHandler {
  /** Instantiates this class */
  public CleanupHandler() {
    super(null);
  }
  static final int MAX_PASSES = 20;
  @Override public Void execute(@SuppressWarnings("unused") final ExecutionEvent e) throws ExecutionException {
    final StringBuilder message = new StringBuilder();
    final ICompilationUnit currentCompilationUnit = currentCompilationUnit();
    final IJavaProject javaProject = currentCompilationUnit.getJavaProject();
    message.append("starting at " + currentCompilationUnit.getElementName() + "\n");
    final List<ICompilationUnit> us = getAllCompilationUnits(currentCompilationUnit);
    message.append("found " + us.size() + " compilation units \n");
    final IWorkbench wb = PlatformUI.getWorkbench();
    final int initialCount = countSuggestions(currentCompilationUnit);
    message.append("with " + initialCount + " suggestions");
    if (initialCount == 0)
      return announce("No suggestions for '" + javaProject.getElementName() + "' project\n" + message);
    for (int i = 0; i < MAX_PASSES; ++i) {
      final IProgressService ps = wb.getProgressService();
      final AtomicInteger passNum = new AtomicInteger(i + 1);
      try {
        ps.busyCursorWhile(new IRunnableWithProgress() {
          @Override public void run(final IProgressMonitor pm) {
            pm.beginTask("Spartanizing project '" + javaProject.getElementName() + "' - " + //
                "Pass " + passNum.get() + " out of maximum of " + MAX_PASSES, us.size());
            int n = 0;
            for (final ICompilationUnit u : us) {
              applySafeSpartanizationsTo(u);
              pm.worked(1);
              pm.subTask(u.getElementName() + " " + ++n + "/" + us.size());
            }
            pm.done();
          }
        });
      } catch (final InvocationTargetException x) {
        x.printStackTrace();
      } catch (final InterruptedException x) {
        x.printStackTrace();
      }
      final int finalCount = countSuggestions(currentCompilationUnit);
      if (finalCount <= 0)
        return announce("Spartanizing '" + javaProject.getElementName() + "' project \n" + //
            "Completed in " + (1 + i) + " passes. \n" + //
            "Total changes: " + (initialCount - finalCount) + "\n" + //
            "Suggestions before: " + initialCount + "\n" + //
            "Suggestions after: " + finalCount + "\n" + //
            message);
    }
    throw new ExecutionException("Too many iterations");
  }
  private static List<ICompilationUnit> getAllCompilationUnits(final ICompilationUnit u) {
    try {
      return Spartanization.getAllProjectCompilationUnits(u, new NullProgressMonitor());
    } catch (final JavaModelException x) {
      x.printStackTrace();
      return null;
    }
  }
  /**
   * Returns the number of Spartanizaion suggestions for this compilation unit
   *
   * @param u JD
   * @return the number of suggesions available for the compilation unit
   */
  public static int countSuggestions(final ICompilationUnit u) {
    int $ = 0;
    for (final Spartanization s : ApplySpartanizationHandler.safeSpartanizations) {
      s.setMarker(null);
      s.setCompilationUnit(u);
      $ += s.countSuggestions();
    }
    return $;
  }
}