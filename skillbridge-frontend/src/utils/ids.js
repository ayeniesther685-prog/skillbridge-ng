// Backend DTO shapes for relations aren't confirmed; read ids defensively.
export const idOf = (obj, rel) => obj?.[`${rel}Id`] ?? obj?.[rel]?.id
export const nameOf = (u) => (u ? [u.firstName, u.lastName].filter(Boolean).join(' ') || u.name || u.email : '')
