// implentation of com.intellij.openapi.actionSystem.AnAction
override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val commitWorkflowHandler =
        e.getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER)
    val changes =
        commitWorkflowHandler.ui.getIncludedChanges()
    generateCommitMessage(changes, project)
}
