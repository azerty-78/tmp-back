package com.kobecorporation.tmp_back.interaction.exception

/**
 * Exception levée quand une ressource n'est pas trouvée
 */
class ResourceNotFoundException(message: String) : RuntimeException(message)
