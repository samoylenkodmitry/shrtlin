import okio.Buffer
import okio.ByteString

fun solveChallenge(challenge: Challenge): ProofOfWork =
    ProofOfWork(challenge.challenge, solveChallenge(challenge.challenge, challenge.prefix), challenge.prefix)

fun solveChallenge(
    challenge: String,
    prefix: String,
): String {
    var nonce = 0
    while (true) {
        val solution = "$challenge:$nonce"
        val hashResult = challengeHash(solution)
        if (hashResult.startsWith(prefix)) {
            return solution
        }
        nonce++
    }
}

fun challengeHash(input: String): String {
    val buffer = Buffer()
    buffer.writeUtf8(input)
    val bytes = buffer.readByteArray()
    if (!bytes.contentEquals(input.encodeToByteArray())) {
        throw IllegalStateException("Bytes are not equal")
    }
    return ByteString.of(*bytes).sha256().hex()
}
