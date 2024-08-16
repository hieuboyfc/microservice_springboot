{{/*
Define labels for pod selectors.
*/}}
{{- define "selectorLabels" -}}
app: {{ .Values.appName }}
app.zimji.vn/name: {{ .Values.appName }}
app.zimji.vn/instance: {{ .Values.instance }}
{{- end }}

{{/*
Define a set of common labels for resources.
*/}}
{{- define "labels" -}}
{{- include "selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Values.instance }}
{{- end }}
