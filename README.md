# document_similarity
some methods to compute document similarity

FNLP is applied.

I found the caculation processes may not be multi-thread-friendly because they are dependent rather than independent. In a multi-thread context, function call between dependent functions may cause problem.
