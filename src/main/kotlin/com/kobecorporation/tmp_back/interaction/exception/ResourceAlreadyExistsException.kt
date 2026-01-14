package com.kobecorporation.tmp_back.interaction.exception

/**
 * Exception levée quand une ressource existe déjà (email, username, etc.)
 */
class ResourceAlreadyExistsException(message: String) : RuntimeException(message)
