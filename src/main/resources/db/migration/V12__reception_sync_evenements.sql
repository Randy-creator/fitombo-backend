CREATE TABLE reception_sync_evenement (
    utilisateur_id UUID NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
    operation_id UUID NOT NULL,
    empreinte CHAR(64) NOT NULL,
    evenement_id UUID REFERENCES evenement(id),
    PRIMARY KEY (utilisateur_id, operation_id)
);
