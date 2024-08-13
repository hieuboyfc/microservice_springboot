{{/*
Define a common set of labels for selecting resources.
*/}}
{{- define "selectorLabels" -}}
app: {{ .Values.appName }}
{{- end -}}

{{/*
Define a set of common labels for resources.
*/}}
{{- define "labels" -}}
{{- include "selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Values.instance }}
{{- end -}}
