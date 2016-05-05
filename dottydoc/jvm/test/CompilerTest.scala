package dotty.tools
package dottydoc

import dotc.core.Contexts
import Contexts.{ Context, ContextBase }
import dotc.core.Phases.Phase
import dotc.typer.FrontEnd
import dottydoc.core.Phases.DocPhase

trait DottyTest {
  dotty.tools.dotc.parsing.Scanners // initialize keywords

  implicit var ctx: Contexts.Context = {
    val base = new ContextBase
    import base.settings._
    val ctx = base.initialCtx.fresh
    ctx.setSetting(ctx.settings.YkeepComments, true)
    base.initialize()(ctx)
    ctx
  }

  private def compilerWithChecker(assertion: DocPhase => Unit) = new DottyDocCompiler {
    val docPhase = new DocPhase
    override def phases =
      List(new FrontEnd) ::
      List(docPhase) ::
      List(new Phase {
        def phaseName = "assertionPhase"
        override def run(implicit ctx: Context): Unit = assertion(docPhase)
      }) ::
      Nil
  }

  def checkCompile(source: String)(assertion: DocPhase => Unit): Unit = {
    val c = compilerWithChecker(assertion)
    c.rootContext(ctx)
    val run = c.newRun
    run.compile(source)
  }

  def checkCompile(sources:List[String])(assertion: DocPhase => Unit): Unit = {
    val c = compilerWithChecker(assertion)
    c.rootContext(ctx)
    val run = c.newRun
    run.compile(sources)
  }
}
