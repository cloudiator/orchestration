/*
 * Copyright (c) 2014-2018 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.cloudiator.orchestration.installer.tools.installer;

import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import io.github.cloudiator.domain.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;


public class InstallerHelper {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(InstallerHelper.class);

    static InstallationInstructions getInstallationInstructionsFromServer(Node node, String urlStr) {
        // Copy node info into map
        Map<String,String> nodeInfo = new HashMap<>();
        nodeInfo.put("id", node.id());
        nodeInfo.put("name", node.name());
        nodeInfo.put("type", node.type().name());
        nodeInfo.put("providerId", node.nodeProperties().providerId());
        nodeInfo.put("operatingSystem", node.nodeProperties().operatingSystem().get().operatingSystemFamily().value());
        nodeInfo.put("ip", node.connectTo().ip().toString());
        LOGGER.debug("Node info to send to EMS: {}", nodeInfo);

        // Contact server to retrieve installation instructions for node
        LOGGER.debug("Contacting server: {}", urlStr);
        Response response = ClientBuilder
                .newClient()
                .target(urlStr)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(nodeInfo));
        LOGGER.debug("Server response: {}", response);

        int statusCode = response.getStatus();
        if (statusCode < 200 || statusCode > 299) {
            LOGGER.error("Contacting server failed: status={}, reason={}", statusCode, response.getStatusInfo().getReasonPhrase());
            return null;
        }

        // Convert server response to InstallationInstructions
        InstallationInstructions installationInstructions = response.readEntity(InstallationInstructions.class);
        LOGGER.debug("Response contents: {}", installationInstructions);

        return installationInstructions;
    }

    static void executeInstructions(Node node, RemoteConnection remoteConnection, InstallationInstructions installationInstructions) throws RemoteException {
        // Execute installation instructions
        for (Instruction instruction : installationInstructions.getInstructions()) {
            if (instruction != null) {
                LOGGER.trace(String.format("Executing instruction on node {}: {}", node.id(), instruction));
                if (instruction.getTaskType() == INSTRUCTION_TYPE.LOG) {
                    LOGGER.debug(String.format(instruction.getCommand(), node.id()));
                } else if (instruction.getTaskType() == INSTRUCTION_TYPE.CMD) {
                    execCmd(remoteConnection, instruction.getCommand());
                } else if (instruction.getTaskType() == INSTRUCTION_TYPE.FILE) {
                    writeFile(remoteConnection, instruction.getFileName(), instruction.getContents(), instruction.isExecutable());
                } else if (instruction.getTaskType() == INSTRUCTION_TYPE.CHECK) {
                    int exitCode = execCmd(remoteConnection, instruction.getCommand());
                    if (instruction.isMatch() && exitCode != instruction.getExitCode() ||
                        ! instruction.isMatch() && exitCode == instruction.getExitCode())
                    {
                        LOGGER.warn(String.format(instruction.getContents(), node.id()));
                        break;  // exit instruction execution loop
                    }

                } else {
                    LOGGER.warn(String.format("Unknown instruction {} in installation instructions of node {}", instruction, node.id()));
                }
            }
        }
    }

    static int execCmd(RemoteConnection remoteConnection, String cmd) throws RemoteException {
        LOGGER.debug("execCmd: " + cmd);
        CommandTask installCmd = new CommandTask(remoteConnection, cmd);
        return installCmd.call();
    }

    static int writeFile(RemoteConnection remoteConnection, String file, String contents, boolean executable) throws RemoteException {
        LOGGER.debug("writeFile: " + file + "\n" + contents);
        FileTask fileCmd = new FileTask(remoteConnection, file, contents, executable);
        return fileCmd.call();
    }

    // -----------------------------------------------------------------------------------

    public enum INSTRUCTION_TYPE {LOG, CMD, FILE, CHECK}

    public static class InstallationInstructions {
        private String os;
        private List<Instruction> instructions;

		public InstallationInstructions() {}
		
        public String getOs() {
            return os;
        }

        public Collection<Instruction> getInstructions() {
            return Collections.unmodifiableCollection(instructions);
        }

        public void setOs(String os) {
            this.os = os;
        }

        public void setInstructions(Collection<Instruction> ni) {
            instructions = new ArrayList<>(ni);
        }

        public void appendInstruction(Instruction i) {
            instructions.add(i);
        }

        public String toString() {
            return new StringBuilder("InstallationInstructions { ")
				.append("os=").append(os)
				.append(", instructions=").append(instructions.toString())
				.append(" }")
				.toString();
        }
    }

    public static class Instruction {
        private INSTRUCTION_TYPE taskType;
        private String command;
        private String fileName;
        private String contents;
        private boolean executable;
        private int exitCode;
        private boolean match;

		public Instruction() {}
		
        public Instruction(INSTRUCTION_TYPE type, String cmd) {
            taskType = type;
            command = cmd;
        }

        public Instruction(String file, String contents, boolean executable) {
            taskType = INSTRUCTION_TYPE.FILE;
            fileName = file;
            this.contents = contents;
            this.executable = executable;
        }

        public Instruction(String cmd, int exitCode, boolean match, String message) {
            taskType = INSTRUCTION_TYPE.CHECK;
            command = cmd;
            this.exitCode = exitCode;
            this.match = match;
            this.contents = message;
        }

        public INSTRUCTION_TYPE getTaskType() {
            return taskType;
        }

        public String getCommand() {
            return command;
        }

        public String getFileName() {
            return fileName;
        }

        public String getContents() {
            return contents;
        }

        public boolean isExecutable() {
            return executable;
        }

        public int getExitCode() { return exitCode; }

        public boolean isMatch() { return match; }

        public String toString() {
            return String.format("Instruction { task-type=%s, command=%s, file-name=%s, contents=%s, executable=%b, exitCode=%d, match=%b }",
                    taskType, command, fileName, contents, executable, exitCode, match);
        }
    }
}
