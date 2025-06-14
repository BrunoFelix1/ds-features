package sdProject.controllers;

import sdProject.models.Matricula;
import sdProject.services.interfaces.MatriculaService;
import sdProject.services.impl.MatriculaServiceImpl;

import java.sql.SQLException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class MatriculaController {
    
    private final MatriculaService matriculaService;
    
    public MatriculaController() throws SQLException {
        this.matriculaService = new MatriculaServiceImpl();
    }
    
    public Map<String, Object> matricularAluno(int alunoId, int disciplinaId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Matricula matricula = matriculaService.matricularAluno(alunoId, disciplinaId);
            response.put("status", "success");
            response.put("message", "Matrícula realizada com sucesso");
            response.put("data", matricula);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Erro ao acessar o banco de dados: " + e.getMessage());
        }
        return response;
    }
    
    public Map<String, Object> cancelarMatricula(int alunoId, int disciplinaId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean cancelada = matriculaService.cancelarMatricula(alunoId, disciplinaId);
            if (cancelada) {
                response.put("status", "success");
                response.put("message", "Matrícula cancelada com sucesso");
            } else {
                response.put("status", "error");
                response.put("message", "Não foi possível cancelar a matrícula");
            }
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Erro ao acessar o banco de dados: " + e.getMessage());
        }
        return response;
    }
    
    public Map<String, Object> listarMatriculasPorAluno(int alunoId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Matricula> matriculas = matriculaService.buscarMatriculasPorAluno(alunoId);
            response.put("status", "success");
            //response.put("data", matriculas);
            response.put("matriculas", matriculas);
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Erro ao acessar o banco de dados: " + e.getMessage());
        }
        return response;
    }
    
    public Map<String, Object> listarMatriculasPorDisciplina(int disciplinaId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Matricula> matriculas = matriculaService.buscarMatriculasPorDisciplina(disciplinaId);
            response.put("status", "success");
            response.put("matriculas", matriculas);
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Erro ao acessar o banco de dados: " + e.getMessage());
        }
        return response;
    }

    public Map<String, Object> verificarMatricula(int alunoId, int disciplinaId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean matriculado = matriculaService.verificarMatricula(alunoId, disciplinaId);
            response.put("status", "success");
            response.put("matriculado", matriculado);
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Erro ao acessar o banco de dados: " + e.getMessage());
        }
        return response;
    }
}
