package br.com.elotech.entity.enums;

public enum StatusTarefa {
    TODO,
    IN_PROGRESS,
    DONE;

    public boolean podeTransicionarPara(StatusTarefa proximoStatus) {
        if (this == proximoStatus) {
            return true;
        }

        return switch (this) {
            case TODO -> proximoStatus == IN_PROGRESS;
            case IN_PROGRESS -> proximoStatus == TODO || proximoStatus == DONE;
            case DONE -> proximoStatus == IN_PROGRESS;
        };
    }
}