select
    p.id,
    p.name,
    d.id as department_id,
    d.name as department_name
from
    person p
    inner join
    department d
    on (p.department_id = d.id)
where
    p.id = /*id*/0
