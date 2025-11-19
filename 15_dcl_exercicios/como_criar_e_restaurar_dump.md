# ✅ **Criar um dump**

### **1. Dump de um banco específico (formato SQL)**

```bash
pg_dump -U usuario -h host -d nome_do_banco > backup.sql
```

### **2. Dump em formato customizado (recomendado)**

Permite restaurar objetos individualmente:

```bash
pg_dump -U usuario -h host -d nome_do_banco -F c -f backup.dump
```

### **3. Dump de todos os bancos**

```bash
pg_dumpall -U usuario > backup_completo.sql
```

---

# ✅ **Restaurar um dump**

## **A) Restaurar dump em formato SQL**

```bash
psql -U usuario -h host -d nome_do_banco < backup.sql
```

> Obs.: O banco precisa existir antes da restauração.

Criar o banco, se necessário:

```bash
createdb -U usuario nome_do_banco
```

---

## **B) Restaurar dump em formato customizado (.dump ou .backup)**

Usa o `pg_restore`:

```bash
pg_restore -U usuario -h host -d nome_do_banco backup.dump
```

Se quiser que ele drope e recrie objetos:

```bash
pg_restore -U usuario -h host -d nome_do_banco --clean --create backup.dump
```

---

# 🔐 **Dicas úteis**

### Especificar a porta

```bash
-p 5432
```

### Forçar sobrescrita de objetos

```bash
--clean
```

### Ver conteúdo do dump

```bash
pg_restore --list backup.dump
```

