# Secure-Substring-Search
Efficient sequencing technologies generate a plethora of genomic data available to researchers. To compute a massive genomic dataset, it is often required to outsource the data to the cloud. Before outsourcing, data owners encrypt sensitive data to ensure data confidentiality. Outsourcing helps data owners to eliminate the local storage management problem. Since genome data is large in volume, executing researchers queries securely and efficiently is challenging.

\parttitle{Methods} %if any
In this paper, we propose a method to securely perform substring search and set-maximal search on SNPs datasets using a generalized suffix tree. The proposed method guarantees the following: (1) data privacy, (2) query privacy, and (3) output privacy. It adopts the semi-honest adversary model, and the security of the data is guaranteed through encryption and garbled circuits.

\parttitle{Results} Our experimental results demonstrate that our proposed method can compute a secure substring and set-maximal search against a single-nucleotide polymorphism (SNPs) database of 2184 records (each record contains 10000 SNPs) in 2.3 and 2 seconds, respectively. Furthermore, we compared our results with existing techniques of secure substring and set-maximal search~\cite{IshimakiISY16,shimizu2016efficient},  where we achieved a noteworthy 400 and 7 times improvement (Table \ref{table:comparison}).
\end{abstract}
