/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST Teacher Evaluation.
 *
 * FenixEdu IST Teacher Evaluation is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST Teacher Evaluation is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST Teacher Evaluation.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.fenixedu.teacher.evaluation.domain;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import pt.ist.fenixframework.Atomic;

import com.google.common.io.ByteStreams;

public class FileUploadBean implements Serializable {

    private transient InputStream inputStream;
    private String filename;
    private String displayName;
    private byte[] bytes;
    private FacultyEvaluationProcess facultyEvaluationProcess;
    private TeacherEvaluationProcess teacherEvaluationProcess;
    private TeacherEvaluationFileType teacherEvaluationFileType;

    public FileUploadBean() {
    }

    public FileUploadBean(final FacultyEvaluationProcess facultyEvaluationProcess) {
        this();
        this.facultyEvaluationProcess = facultyEvaluationProcess;
    }

    public FileUploadBean(final TeacherEvaluationProcess teacherEvaluationProcess,
            TeacherEvaluationFileType teacherEvaluationFileType) {
        this();
        this.teacherEvaluationProcess = teacherEvaluationProcess;
        this.teacherEvaluationFileType = teacherEvaluationFileType;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public FacultyEvaluationProcess getFacultyEvaluationProcess() {
        return facultyEvaluationProcess;
    }

    public void setFacultyEvaluationProcess(FacultyEvaluationProcess facultyEvaluationProcess) {
        this.facultyEvaluationProcess = facultyEvaluationProcess;
    }

    public void consumeInputStream() throws IOException {
        bytes = ByteStreams.toByteArray(inputStream);
    }

    @Atomic
    public void uploadEvaluators() {
        facultyEvaluationProcess.uploadEvaluators(bytes);
    }

    @Atomic
    public void uploadApprovedEvaluations() {
        facultyEvaluationProcess.uploadApprovedEvaluations(bytes);
    }

    public TeacherEvaluationProcess getTeacherEvaluationProcess() {
        return teacherEvaluationProcess;
    }

    public void setTeacherEvaluationProcess(TeacherEvaluationProcess teacherEvaluationProcess) {
        this.teacherEvaluationProcess = teacherEvaluationProcess;
    }

    public TeacherEvaluationFileType getTeacherEvaluationFileType() {
        return teacherEvaluationFileType;
    }

    public void setTeacherEvaluationFileType(TeacherEvaluationFileType teacherEvaluationFileType) {
        this.teacherEvaluationFileType = teacherEvaluationFileType;
    }

}
