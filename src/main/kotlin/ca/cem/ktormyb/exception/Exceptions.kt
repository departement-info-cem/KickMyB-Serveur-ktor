package ca.cem.ktormyb.exception

sealed class ServiceException(message: String) : Exception(message)

// User exceptions
class NomTropCourt : ServiceException("NomTropCourt")
class NomTropLong : ServiceException("NomTropLong")
class NomDejaPris : ServiceException("NomDejaPris")
class MotDePasseTropCourt : ServiceException("MotDePasseTropCourt")
class MotDePasseTropLong : ServiceException("MotDePasseTropLong")
class MotsDePasseDifferents : ServiceException("MotsDePasseDifferents")
class MauvaisNomOuMotDePasse : ServiceException("MauvaisNomOuMotDePasse")

// Task exceptions
class Existant : ServiceException("Existant")
class TropCourt : ServiceException("TropCourt")
class TropLong : ServiceException("TropLong")
class Vide : ServiceException("Vide")
