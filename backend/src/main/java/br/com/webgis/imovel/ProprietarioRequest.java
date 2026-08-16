package br.com.webgis.imovel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProprietarioRequest(@NotBlank @Size(max = 120) String nome) {
}
