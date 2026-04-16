CREATE TABLE named_expression (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(200) NOT NULL UNIQUE,
    spel  TEXT NOT NULL
);

ALTER TABLE abac_expression
  ADD COLUMN name                VARCHAR(200) NULL,
  ADD COLUMN named_expression_id BIGINT       NULL,
  ADD CONSTRAINT fk_expr_named
    FOREIGN KEY (named_expression_id) REFERENCES named_expression(id);
