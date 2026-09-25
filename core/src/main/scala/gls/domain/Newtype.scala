package gls.domain

trait Newtype[Wrapped, Raw](using Wrapped =:= Raw) extends NewtypeWrap[Wrapped, Raw] with NewtypeUnwrap[Wrapped, Raw]

trait NewtypeWrap[Wrapped, Raw](using ev: Wrapped =:= Raw) {
  def apply(raw: Raw): Wrapped = ev.flip(raw)
}

trait NewtypeUnwrap[Wrapped, Raw](using ev: Wrapped =:= Raw) {
  extension (wrapped: Wrapped) def unwrap: Raw = ev(wrapped)
}
