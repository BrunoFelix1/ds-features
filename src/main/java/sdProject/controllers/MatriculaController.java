package sdProject.controllers;

import sdProject.models.Matricula;
import sdProject.services.interfaces.MatriculaService;
import sdProject.services.impl.MatriculaServiceImpl;
import sdProject.dto.MatriculaDTO;
import sdProject.dto.ResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

public class MatriculaController implements AutoCloseable {
    
    private final MatriculaService matriculaService;
    
    public MatriculaController() {
        this.matriculaService = new MatriculaServiceImpl();
    }
    
    public ResponseDTO<MatriculaDTO> matricularAluno(int alunoId, int disciplinaId) {
        try {
            Matricula matricula = matriculaService.matricularAluno(alunoId, disciplinaId);
            MatriculaDTO dto = convertToDTO(matricula);
            return ResponseDTO.success("Matrícula realizada com sucesso", dto);
        } catch (IllegalArgumentException e) {
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            return ResponseDTO.error("Erro ao processar matrícula: " + e.getMessage());
        }
    }
    
    public ResponseDTO<Boolean> cancelarMatricula(int alunoId, int disciplinaId) {
        try {
            boolean cancelada = matriculaService.cancelarMatricula(alunoId, disciplinaId);
            if (cancelada) {
                return ResponseDTO.success("Matrícula cancelada com sucesso", true);
            } else {
                return ResponseDTO.error("Não foi possível cancelar a matrícula");
            }
        } catch (Exception e) {
            return ResponseDTO.error("Erro ao cancelar matrícula: " + e.getMessage());
        }
    }
    
    public ResponseDTO<List<MatriculaDTO>> listarMatriculasPorAluno(int alunoId) {
        try {
            List<Matricula> matriculas = matriculaService.buscarMatriculasPorAluno(alunoId);
            List<MatriculaDTO> dtos = matriculas.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            return ResponseDTO.success(dtos);
        } catch (Exception e) {
            return ResponseDTO.error("Erro ao listar matrículas: " + e.getMessage());
        }
    }
    
    public ResponseDTO<List<MatriculaDTO>> listarMatriculasPorDisciplina(int disciplinaId) {
        try {
            List<Matricula> matriculas = matriculaService.buscarMatriculasPorDisciplina(disciplinaId);
            List<MatriculaDTO> dtos = matriculas.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            return ResponseDTO.success(dtos);
        } catch (Exception e) {
            return ResponseDTO.error("Erro ao listar matrículas: " + e.getMessage());
        }
    }

    public ResponseDTO<Boolean> verificarMatricula(int alunoId, int disciplinaId) {
        try {
            boolean matriculado = matriculaService.verificarMatricula(alunoId, disciplinaId);
            return ResponseDTO.success(matriculado);
        } catch (Exception e) {
            return ResponseDTO.error("Erro ao verificar matrícula: " + e.getMessage());
        }
    }

    private MatriculaDTO convertToDTO(Matricula matricula) {
        if (matricula == null) return null;
        MatriculaDTO dto = new MatriculaDTO();
        dto.setId(matricula.getId());
        dto.setAlunoId(matricula.getAlunoId());
        dto.setDisciplinaId(matricula.getDisciplinaId());
        dto.setNota(matricula.getNota());
        return dto;
    }

    @Override
    public void close() throws Exception {
        matriculaService.close();
    }
}
