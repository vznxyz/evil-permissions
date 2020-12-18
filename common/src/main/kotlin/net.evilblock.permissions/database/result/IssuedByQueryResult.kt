package net.evilblock.permissions.database.result

import net.evilblock.permissions.user.User
import net.evilblock.permissions.user.grant.Grant

class IssuedByQueryResult(val user: User, val grant: Grant)