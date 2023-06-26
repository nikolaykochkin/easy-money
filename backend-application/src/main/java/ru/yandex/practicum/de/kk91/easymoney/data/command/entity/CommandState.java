package ru.yandex.practicum.de.kk91.easymoney.data.command.entity;

public enum CommandState {
    NEW,
    LOADING,
    LOADED,
    VOICE_PROCESSING,
    NLP,
    IMAGE_PROCESSING,
    INVOICE_PARSING,
    REPORT_GENERATING,
    DRAFT_CONFIRMATION,
    CLOSED,
    ERROR
}
