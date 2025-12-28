# OpenType 読み込みライブラリ
バージョン 1.1.0

Apache Batik 1.7のTrueType読み込みパッケージ `org.apache.batik.svggen.font` のパフォーマンスを向上させ、OpenType/CFF(.otfファイル)に対応させたものです。
既存のBatikライブラリとの衝突を防ぐため、パッケージ名を変えています。

このライブラリを使用することで、OpenTypeフォントのメタ情報やグリフの形状を抽出することが出来ます。
OpenTypeフォントについて理解するには、以下のサイトを参照してください。
http://www.microsoft.com/typography/otspec/

## ビルド方法

ビルドには Gradle を使用します。
ルートディレクトリで `./gradlew :zs-resolver:build` を実行してください。

## ライセンス

このソフトウェアはApache Software FoundationのApache Batik (http://xmlgraphics.apache.org/batik/) から派生したものであり、Batikの再配布条件に従います。

オリジナルのBatikのライセンスは、付属の `LICENSE.batik` をご参照ください。

このソフトウェアはWorld Wide Web Consortium (W3C)のSAC API 1.3のJava APIのライブラリを含んでいます。
