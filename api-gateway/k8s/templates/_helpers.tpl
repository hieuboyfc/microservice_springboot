{{/*
Define labels for pod selectors.
*/}}
{{- define "selectorLabels" -}}
app.kubernetes.io/name: {{ .Values.appName }}
app.kubernetes.io/instance: {{ .Values.instance }}
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
