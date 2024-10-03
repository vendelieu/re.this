import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph

fun Project.disableUnreachableTasks() {
    require(rootProject == this) { "Must be called on a root project" }

    gradle.taskGraph.whenReady {
        DisableTasks(graph = this)
            .disableTasks()
    }
}

private class DisableTasks(
    private val graph: TaskExecutionGraph,
) {
    private val rootTasks = findRootTasks()
    private val results = HashMap<Pair<Task, Task>, Boolean>()

    private fun findRootTasks(): List<Task> {
        val rootTasks = ArrayList<Task>()

        val children = HashSet<Task>()
        graph.allTasks.forEach {
            children += graph.getDependencies(it)
        }

        graph.allTasks.forEach {
            if (it !in children) {
                rootTasks += it
            }
        }

        return rootTasks
    }

    fun disableTasks() {
        graph
            .allTasks
            .filterNot { it.enabled }
            .forEach { disableChildren(it) }
    }

    private fun disableChildren(task: Task) {
        graph.getDependencies(task).forEach { child ->
            if (child.enabled) {
                if (!isTaskAccessible(task = child)) {
                    child.enabled = false
                    disableChildren(task = child)
                }
            } else {
                disableChildren(task = child)
            }
        }
    }

    private fun isTaskAccessible(task: Task): Boolean =
        rootTasks.any { (it != task) && isPathExists(source = it, destination = task) }

    private fun isPathExists(source: Task, destination: Task): Boolean =
        results.getOrPut(source to destination) {
            when {
                !source.enabled -> false
                source == destination -> true
                else -> graph.getDependencies(source).any { isPathExists(source = it, destination = destination) }
            }
        }
}
