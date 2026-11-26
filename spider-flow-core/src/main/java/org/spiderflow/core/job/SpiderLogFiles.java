package org.spiderflow.core.job;

import java.io.File;

public final class SpiderLogFiles {

	private SpiderLogFiles() {
	}

	public static File taskLogForWrite(String workspace, String flowId, Integer taskId) {
		return resolveTaskLog(workspace, flowId, String.valueOf(taskId));
	}

	public static File findTaskLog(String workspace, String flowId, String taskId) {
		if (flowId == null || !flowId.matches("[A-Za-z0-9_-]+") || taskId == null || !taskId.matches("[0-9]+")) {
			return null;
		}
		File file = resolveTaskLog(workspace, flowId, taskId);
		return file.isFile() ? file : null;
	}

	public static File requestErrorLog(String workspace, String flowId) {
		return new File(flowLogDirectory(workspace, flowId), "access_error.log");
	}

	private static File resolveTaskLog(String workspace, String flowId, String taskId) {
		String fileName = taskId + ".log";
		File current = new File(flowLogDirectory(workspace, flowId), fileName);
		File previous = new File(new File(new File(workspace, flowId), "logs"), fileName);
		// 已存在的任务日志继续读取和追加到同一文件
		return !current.isFile() && previous.isFile() ? previous : current;
	}

	private static File flowLogDirectory(String workspace, String flowId) {
		return new File(new File(workspace, "logs"), flowId);
	}
}
