import { initials } from '../../utils/format'

export default function Avatar({ user, size = 32 }) {
  return user?.profilePicture
    ? (
      <img
        src={user.profilePicture}
        alt=""
        style={{ width: size, height: size }}
        className="rounded-full object-cover ring-2 ring-primary/20"
      />
    )
    : (
      <span
        style={{ width: size, height: size }}
        className="inline-flex items-center justify-center rounded-full bg-gradient-to-br from-primary to-accent text-xs font-semibold text-white"
      >
        {initials(user)}
      </span>
    )
}