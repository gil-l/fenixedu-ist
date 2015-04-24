/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST Integration.
 *
 * FenixEdu IST Integration is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST Integration is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST Integration.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.fenixedu.integration.task.updateData.enrolment;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.student.Student;
import org.fenixedu.academic.domain.util.email.Message;
import org.fenixedu.academic.domain.util.email.Sender;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.UserGroup;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.io.domain.GenericFile;
import org.fenixedu.bennu.scheduler.custom.CustomTask;

import pt.ist.fenixframework.FenixFramework;
import edu.emory.mathcs.backport.java.util.Collections;

public class SendEmailToLowPerformanceStudents extends CustomTask {

    @Override
    public void runTask() throws Exception {
        User user = User.findByUsername("ist24616");
        Authenticate.mock(user);

        GenericFile file = FenixFramework.getDomainObject("566729524642563"); //file with the students numbers, one in each line

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(file.getContent())));
        String line;
        Set<Person> students = new HashSet<Person>();
        while ((line = br.readLine()) != null) {
            Student student = Student.readStudentByNumber(Integer.valueOf(line));
            if (student == null) {
                taskLog("Can't find student -> " + line);
                continue;
            }
            students.add(student.getPerson());
        }
        br.close();

        createEmail(students);

        taskLog("Done.");
    }

    private Sender getConcelhoDeGestaoSender() {
        return FenixFramework.getDomainObject("4196183080395");
    }

    private void createEmail(final Set<Person> students) {

        Sender sender = getConcelhoDeGestaoSender();

        final Set<String> bccs = new HashSet<String>();
        bccs.add("marta.graca@ist.utl.pt");

        new Message(sender, null, Collections.singletonList(UserGroup.of(Person.convertToUsers(students))), getSubject(),
                getBody(), bccs);
        taskLog("Sent: " + students.size() + " emails");
    }

    private String getSubject() {
        return "Baixo rendimento académico";
    }

    private String getBody() {
        StringBuilder builder = new StringBuilder();

        builder.append("Caro aluno do IST,\n");
        builder.append("Apesar de não constar da lista de prescrições em 2014/205, verificou-se que o seu rendimento académico tem sido claramente abaixo da média.\n");
        builder.append("\n");
        builder.append("Por forma a evitar a sua prescrição no próximo ano é aconselhado a:\n");
        builder.append("\n");
        builder.append("1 - ler atentamente o regulamento de prescrições do IST que poderá descarregar em http://www.ist.utl.pt/pt/alunos;\n");
        builder.append("\n");
        builder.append("2 - alterar a sua inscrição em 2014/2015 para o regime de tempo parcial (consulte a Parte II do Guia Académico em http://www.ist.utl.pt/pt/alunos);\n");
        builder.append("\n");
        builder.append("3 - contactar durante o próximo ano lectivo o Gabinete de Apoio ao Tutorado (GATu).\n");
        builder.append("\n");
        builder.append("Com os melhores cumprimentos e votos de um bom ano escolar de 2014/2015,\n");
        builder.append("\n");
        builder.append("Prof. Jorge Morgado");
        builder.append("\n");
        builder.append("Conselho de Gestão do Instituto Superior Técnico,");
        builder.append("\n");
        builder.append("Assuntos Académicos\n");

        return builder.toString();
    }
}